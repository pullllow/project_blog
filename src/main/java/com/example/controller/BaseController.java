package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.UserCollection;
import com.example.service.*;
import com.example.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

/*
 *  @author changqi
 *  @date 2022/3/11 22:03
 *  @description
 *  @Version V1.0
 */

public class BaseController {

    @Autowired
    HttpServletRequest req;

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserCollectionService userCollectionService;



    public Page getPage(){
        //当前页码
        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        //当前页 数据数量
        int size = ServletRequestUtils.getIntParameter(req, "size", 5);
        return new Page(pn, size);
    }


    public AccountProfile getProfile() {
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    protected Long getProfileId() {
        return getProfile().getId();
    }

}
