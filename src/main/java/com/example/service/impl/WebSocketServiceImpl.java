package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.UserMessage;
import com.example.service.UserMessageService;
import com.example.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Chang Qi
 * @date 2022/4/27 20:52
 * @description
 * @Version V1.0
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    UserMessageService messageService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Async
    @Override
    public void sendMessCountToUser(Long toUserId) {
        int count = messageService.count(new QueryWrapper<UserMessage>()
                .eq("to_user_id", toUserId)
                .eq("status", "0")
        );

        // websocket通知 (/user/20/messCount)
        messagingTemplate.convertAndSendToUser(toUserId.toString(), "/messCount", count);
        log.info("WebSocket发送消息成功-------->用户：{}，数量:{}", toUserId, count);
    }
}
