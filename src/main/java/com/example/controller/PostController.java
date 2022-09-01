package com.example.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.bussniess.post.delete.DeleteThread;
import com.example.bussniess.post.reply.ReplyThread;
import com.example.common.lang.Result;
import com.example.config.RabbitMqConfig;
import com.example.entity.*;
import com.example.search.common.PostMqIndexMessage;
import com.example.service.RedisLockService;
import com.example.util.RedisUtil;
import com.example.util.ValidationUtil;
import com.example.vo.CommentVo;
import com.example.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.Future;

@Controller
@Slf4j
public class PostController extends BaseController {


    @Autowired
    @Qualifier("deleteThreadPool")
    ThreadPoolTaskExecutor deleteThreadPool;

    @Autowired
    RedisLockService redisLockService;

    @Autowired
    RedisUtil redisUtil;


    @Autowired
    @Qualifier("replyThreadPool")
    ThreadPoolTaskExecutor replyThreadPool;

    @GetMapping("/category/{id:\\d*}")
    public String category(@PathVariable(name = "id") Long id) {
        //当前页码
        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        req.setAttribute("currentCategoryId", id);
        req.setAttribute("pn", pn);
        return "post/category";
    }

    @GetMapping("/post/{id:\\d*}")
    public String post(@PathVariable(name = "id") Long id) {



        PostVo vo = postService.selectOnePost(new QueryWrapper<Post>().eq("p.id", id));
        Assert.notNull(vo, "文章已被删除");

        postService.putViewCount(vo);

        // 1分页 2文章id 3用户id 4排序
        IPage<CommentVo> results = commentService.paging(getPage(), vo.getId(), null, "created");

        req.setAttribute("currentCategoryId", vo.getCategoryId());
        req.setAttribute("post", vo);
        req.setAttribute("pageData", results);


        return "post/detail";
    }

    @Transactional
    @GetMapping("/post/edit")
    public String edit() {
        String id = req.getParameter("id");
        String token = "";

        //获取分布式锁
        try {
            token = redisLockService.lock(id.toString(), 1000, 11000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(token!=null) {
                if (!StringUtils.isEmpty(id)) {
                    Post post = postService.getById(id);
                    Assert.isTrue(post != null, "该帖子已经被删除");
                    Assert.isTrue(post.getUserId().longValue() == getProfileId(), "没有权限操作此文章");
                    req.setAttribute("post", post);
                }

                req.setAttribute("categories", categoryService.list());
                Boolean unlock = redisLockService.unlock(id.toString(), token);
                if(!unlock) {
                    log.error("分布式锁释放失败");
                }
            }
        }
        return "/post/edit";
    }

    @Transactional
    @ResponseBody
    @PostMapping("/post/submit")
    public Result submit(@Valid Post post, String vercode) {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(post);

        if (!"2".equals(vercode)) {
            return Result.fail("人类认证错误！");
        }

        if (validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }

        Date date = new Date();

        if (post.getId() == null) {
            //发表博客
            post.setUserId(getProfileId());
            post.setCreated(date);
            post.setModified(date);

            post.setCommentCount(0);
            post.setEditMode(null);
            post.setLevel(0);
            post.setRecommend(false);

            post.setViewCount(0);
            post.setVoteDown(0);
            post.setVoteUp(0);

        } else {
            //编辑博客
            Post tempPost = postService.getById(post.getId());
            Assert.isTrue(tempPost.getUserId().longValue() == getProfileId().longValue(), "无权限编辑此文章");

        }

        postService.saveOrUpdate(post);

        //System.out.println(1/0);

        // 通知消息给mq，告知更新或添加
        amqpTemplate.convertAndSend(RabbitMqConfig.ES_EXCHANGE, RabbitMqConfig.ES_BIND_KEY,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.CREATE_OR_UPDATE));

        return Result.success("发表成功").action("/post/" + post.getId());
    }


    @Transactional
    @ResponseBody
    @PostMapping("/post/delete")
    public Result delete(Long id) {

        DeleteThread thread;
        Result result = null;
        try {
            thread = new DeleteThread(id, getProfileId());
            Future<Result> future = deleteThreadPool.submit(thread);
            result = future.get();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
             return result.action("/user/home");

        }

    }


    @Transactional
    @ResponseBody
    @PostMapping("/post/reply")
    public Result reply(Long postId, String content) {

        Assert.notNull(postId, "找不到对应的博客");
        Assert.hasLength(content, "评论内容不能为空");

        String token = "";

        Result result = null;
        //获取分布式锁
        try {

            token = redisLockService.lock(postId.toString(), 1000, 11000);

            ReplyThread replyThread = new ReplyThread(postId, getProfileId(),content);

            Future<Result> resultFuture = replyThreadPool.submit(replyThread);

            result = resultFuture.get();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if(token!=null) {
                Boolean unlock = redisLockService.unlock(postId.toString(), token);
                if(!unlock) {
                    log.error("分布式锁释放失败");
                }
            }
        }

        return result.action("/post/" + postId);
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/jieda-delete/")
    public Result reply(Long id) {

        Assert.notNull(id, "评论id不能为空！");

        Comment comment = commentService.getById(id);

        Assert.notNull(comment, "找不到对应评论！");

        if (comment.getUserId().longValue() != getProfileId().longValue()) {
            return Result.fail("不是你发表的评论！");
        }
        commentService.removeById(id);

        // 评论数量减一
        Post post = postService.getById(comment.getPostId());
        post.setCommentCount(post.getCommentCount() - 1);
        postService.saveOrUpdate(post);

        //评论数量减一
        postService.incrCommentCountAndUnionForWeekRank(comment.getPostId(), false);

        return Result.success(null);
    }


    @ResponseBody
    @PostMapping("/collection/find/")
    public Result collectionFind(Long cid) {

        int count = userCollectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", cid)
        );


        return Result.success(MapUtil.of("collection", count > 0));
    }

    @ResponseBody
    @PostMapping("/collection/add/")
    public Result collectionAdd(Long cid) {
        Post post = postService.getById(cid);

        Assert.isTrue(post!=null,"该帖子已经被删除");

        int count = userCollectionService.count(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", cid)
        );

        if(count>0) {
            return Result.fail("你已经收藏该帖子");
        }

        Date date = new Date();

        UserCollection collection = new UserCollection();

        collection.setPostId(cid);

        collection.setUserId(getProfileId());
        collection.setPostUserId(post.getUserId());

        collection.setCreated(date);
        collection.setModified(date);

        userCollectionService.save(collection);

        return Result.success();
    }

    @ResponseBody
    @PostMapping("/collection/remove/")
    public Result collectionRemove(Long cid) {
        Post post = postService.getById(cid);
        Assert.isTrue(post != null, "该帖子已被删除");

        userCollectionService.remove(new QueryWrapper<UserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", cid));

        return Result.success();

    }


}
