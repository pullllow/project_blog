package com.example.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.lang.Result;
import com.example.entity.Post;
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


}
