package com.example.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.example.common.lang.Consts;
import com.example.im.vo.ImMsg;
import com.example.im.vo.ImUser;
import com.example.service.ChatService;
import com.example.shiro.AccountProfile;
import com.example.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:52
 * @description
 * @Version V1.0
 */
@Slf4j
@Service("chatService")
public class ChatServiceImpl implements ChatService {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public ImUser getCurrentUser() {
        //获取当前用户
        AccountProfile profile = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        ImUser user = new ImUser();

        if (profile != null) {
            //登录用户
            user.setId(profile.getId());
            user.setAvatar(profile.getAvatar());
            user.setUsername(profile.getUsername());
        } else {

            user.setAvatar("http://tp1.sinaimg.cn/5619439268/180/40030060651/1");

            //匿名用户处理
            Long imUserId = (Long) SecurityUtils.getSubject().getSession().getAttribute("imUserId");
            user.setId(imUserId != null ? imUserId : RandomUtil.randomLong());

            SecurityUtils.getSubject().getSession().setAttribute("imUserId", user.getId());

            user.setSign("该用户没有个人签名");
            user.setUsername("匿名用户");
        }
        user.setStatus(ImUser.ONLINE_STATUS);

        return user;
    }

    @Override
    public void setGroupHistoryMsg(ImMsg imMsg) {
        redisUtil.lSet(Consts.IM_GROUP_HISTORY_MSG_KEY,imMsg, 24*60*60);
    }

    @Override
    public List<Object> getGroupHistoryMsg(int count) {
        long length = redisUtil.lGetListSize(Consts.IM_GROUP_HISTORY_MSG_KEY);
        return redisUtil.lGet(Consts.IM_GROUP_HISTORY_MSG_KEY, length - count < 0 ? 0 : length - count, length);
    }

}
