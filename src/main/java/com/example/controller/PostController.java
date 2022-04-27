package com.example.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.lang.Result;
import com.example.entity.Comment;
import com.example.entity.Post;
import com.example.entity.UserMessage;
import com.example.util.ValidationUtil;
import com.example.vo.CommentVo;
import com.example.vo.PostVo;
import org.apache.commons.lang3.StringUtils;
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

@Controller
public class PostController extends BaseController {

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
        req.setAttribute("pageData",results);


        return "post/detail";
    }

    @GetMapping("/post/edit")
    public String edit() {
        String id = req.getParameter("id");

        if(!StringUtils.isEmpty(id)) {
            Post post = postService.getById(id);
            Assert.isTrue(post!=null, "该帖子已经被删除");
            Assert.isTrue(post.getUserId().longValue()==getProfileId(),"没有权限操作此文章");
            req.setAttribute("post",post);
        }

        req.setAttribute("categories",categoryService.list());


        return "/post/edit";
    }

    @Transactional
    @ResponseBody
    @PostMapping("/post/submit")
    public Result submit(@Valid Post post, String vercode) {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(post);

        if(!"2".equals(vercode)) {
            return Result.fail("人类认证错误！");
        }

        if(validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }

        Date date = new Date();

        if(post.getId()==null) {
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
            Assert.isTrue(tempPost.getUserId().longValue()==getProfileId().longValue(),"无权限编辑此文章");

        }

        postService.saveOrUpdate(post);


        return Result.success("发表成功").action("/post/"+post.getId());
    }


    @ResponseBody
    @PostMapping("/post/delete")
    public Result delete(Long id) {

        Post post = postService.getById(id);

        Assert.notNull(post, "该帖子已经被删除");
        Assert.isTrue(post.getUserId().longValue()==getProfileId().longValue(),"无权限删除此文章");

        postService.removeById(id);

        //删除相关消息、收藏等
        userMessageService.removeByMap(MapUtil.of("post_id",id));
        userCollectionService.removeByMap(MapUtil.of("post_id",id));




        return Result.success().action("/user/home");
    }


    @ResponseBody
    @PostMapping("/post/reply")
    public Result reply(Long postId, String content) {

        Assert.notNull(postId,"找不到对应的博客");
        Assert.hasLength(content,"评论内容不能为空");

        Post post = postService.getById(postId);
        Assert.isTrue(post != null, "该文章已被删除");

        Date date = new Date();

        Comment comment = new Comment();

        comment.setPostId(postId);
        comment.setContent(content);

        comment.setUserId(getProfileId());

        comment.setCreated(date);
        comment.setModified(date);

        comment.setLevel(0);
        comment.setVoteDown(0);
        comment.setVoteUp(0);

        commentService.save(comment);

        //博客评论数量+1
        post.setCommentCount(post.getCommentCount()+1);
        postService.updateById(post);

        //本周热议数量+1

        postService.incrCommentCountAndUnionForWeekRank(postId,true);

        //通知作者，博客被评论 (自己评论自己不通知)
        if(comment.getUserId()!=post.getUserId()) {
            UserMessage message = new UserMessage();

            message.setPostId(postId);
            ///
            message.setCommentId(comment.getId());

            message.setFromUserId(getProfileId());
            message.setToUserId(post.getUserId());

            message.setType(1);
            message.setContent(content);
            message.setCreated(date);
            message.setModified(date);

            message.setStatus(0);

            userMessageService.save(message);

        }


        return Result.success().action("/post/"+postId);
    }

    @ResponseBody
    @Transactional
    @PostMapping("/post/jieda-delete/")
    public Result reply(Long id) {

        Assert.notNull(id, "评论id不能为空！");

        Comment comment = commentService.getById(id);

        Assert.notNull(comment, "找不到对应评论！");

        if(comment.getUserId().longValue() != getProfileId().longValue()) {
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



}
