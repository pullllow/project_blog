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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.lang.Result;
import com.example.entity.Post;
import com.example.entity.User;
import com.example.entity.UserMessage;
import com.example.shiro.AccountProfile;
import com.example.util.UploadUtil;
import com.example.vo.UserMessageVo;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class UserController extends BaseController {

    @Autowired
    UploadUtil uploadUtil;

    @GetMapping("/user/{id:\\d*}")
    public String userHome(@PathVariable(name = "id") Long id) {
        User user = userService.getById(id);

        //获取30天内发表的
        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .eq("user_id", id)
                .gt("created", DateUtil.offsetDay(new Date(), -30))
                .orderByDesc("created")
        );

        req.setAttribute("user", user);
        req.setAttribute("posts", posts);
        return "/user/home";
    }


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

    @ResponseBody
    @PostMapping("/user/repass")
    public Result repass(String nowpass, String pass, String repass) {
        if (!pass.equals(repass)) {
            return Result.fail("两次密码不相同");
        }

        User user = userService.getById(getProfileId());
        String nowPassMd5 = SecureUtil.md5(nowpass);
        if (!nowPassMd5.equals(user.getPassword())) {
            return Result.fail("密码不正确");
        }

        user.setPassword(SecureUtil.md5(pass));
        userService.updateById(user);

        return Result.success().action("/user/set#pass");

    }



    @GetMapping("/user/index")
    public String index() {
        return "/user/index";
    }


    @GetMapping("/user/set")
    public String set() {
        User user = userService.getById(getProfileId());

        req.setAttribute("user", user);

        return "/user/set";
    }


    @ResponseBody
    @GetMapping("/user/public")
    public Result userPublic() {
        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .eq("user_id", getProfileId())
                .orderByDesc("created"));

        return Result.success(page);
    }

    @ResponseBody
    @GetMapping("/user/collection")
    public Result collection() {

        IPage page = postService.page(getPage(), new QueryWrapper<Post>()
                .inSql("id", "SELECT post_id FROM user_collection where user_id=" + getProfileId())
        );

        return Result.success(page);
    }


    @ResponseBody
    @PostMapping("/user/set")
    public Result doSet(User user) {

        if (StrUtil.isNotBlank(user.getAvatar())) {
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


    @GetMapping("/user/mess")
    public String message() {

        IPage<UserMessageVo> page = userMessageService.paging(getPage(), new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileId())
                .orderByAsc("status")
                .orderByDesc("created")
        );

        //将所有消息标记为已读
        List<Long> ids = new ArrayList<>();
        for (UserMessageVo messageVo : page.getRecords()) {
            if(messageVo.getStatus()==0) {
                ids.add(messageVo.getId());
            }
        }

        //批量将消息修改为已读

        userMessageService.updateToRead(ids);


        req.setAttribute("pageData",page);

        return "/user/mess";
    }



    @ResponseBody
    @PostMapping("/mess/remove")
    public Result remove(Long id,
                         @RequestParam(defaultValue = "false") Boolean all) {

        boolean remove = userMessageService.remove(new QueryWrapper<UserMessage>()
                .eq("to_user_id", getProfileId())
                .eq(!all, "id", id)
        );

        return remove? Result.success():Result.fail("删除失败");
    }




}
