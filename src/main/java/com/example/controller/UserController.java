package com.example.controller;
/*
 *  @author changqi
 *  @date 2022/3/31 14:35
 *  @description
 *  @Version V1.0
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.lang.Result;
import com.example.entity.Post;
import com.example.entity.User;
import com.example.shiro.AccountProfile;
import com.example.util.UploadUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

@Controller
public class UserController extends BaseController {

    @Autowired
    UploadUtil uploadUtil;


    @GetMapping("/user/home")
    public String home() {
        User user = userService.getById(getProfileId());

        //获取30天内发表的
        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .eq("user_id", getProfileId())
                .gt("created", DateUtil.offsetDay(new Date(), -30))
                .orderByDesc("created")
        );

        req.setAttribute("user", user);
        req.setAttribute("posts", posts);


        return "/user/home";
    }

    @GetMapping("/user/set")
    public String set() {
        User user = userService.getById(getProfileId());

        req.setAttribute("user", user);

        return "/user/set";
    }


    @ResponseBody
    @PostMapping("/user/set")
    public Result doSet(User user) {

        if(StrUtil.isNotBlank(user.getAvatar())) {
            User temp = userService.getById(getProfileId());
            temp.setAvatar(user.getAvatar());
            userService.updateById(temp);

            AccountProfile profile = getProfile();
            profile.setAvatar(user.getAvatar());

            SecurityUtils.getSubject().getSession().setAttribute("profile", profile);

            return Result.success().action("/user/set#avatar");

        }

        if (StrUtil.isBlank(user.getUsername())) {
            return Result.fail("昵称不能为空");
        }
        int count = userService.count(new QueryWrapper<User>()
                .eq("username", user.getUsername())
                .ne("id", getProfileId())
        );
        if (count > 0) {
            return Result.fail("该昵称已被占用");
        }
        User temp = userService.getById(getProfileId());

        temp.setUsername(user.getUsername());
        temp.setGender(user.getGender());
        temp.setSign(user.getSign());

        userService.updateById(temp);

        //shiro权限中的user信息更新
        AccountProfile profile = getProfile();
        profile.setUsername(temp.getUsername());
        profile.setSign(temp.getSign());


        return Result.success().action("/user/set#info");
    }


    @ResponseBody
    @PostMapping("/user/upload")
    public Result uploadAvatar(@RequestParam(value = "file") MultipartFile file) throws IOException {
        return uploadUtil.upload(UploadUtil.type_avatar, file);
    }



    @ResponseBody
    @PostMapping("/user/repass")
    public Result repass(String nowpass, String pass, String repass) {
        if(!pass.equals(repass)) {
            return Result.fail("两次密码不相同");
        }

        User user = userService.getById(getProfileId());
        String nowPassMd5 = SecureUtil.md5(nowpass);
        if(!nowPassMd5.equals(user.getPassword())) {
            return Result.fail("密码不正确");
        }

        user.setPassword(SecureUtil.md5(pass));
        userService.updateById(user);

        return Result.success().action("/user/set#pass");

    }

}
