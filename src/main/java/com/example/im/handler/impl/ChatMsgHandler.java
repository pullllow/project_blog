package com.example.im.handler.impl;

import cn.hutool.json.JSONUtil;
import com.example.common.lang.Consts;
import com.example.im.handler.MsgHandler;
import com.example.im.handler.filter.ExcludeMineChannelContextFilter;
import com.example.im.message.ChatInMsg;
import com.example.im.message.ChatOutMsg;
import com.example.im.vo.ImMsg;
import com.example.im.vo.ImTo;
import com.example.im.vo.ImUser;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;

import java.util.Date;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:03
 * @description
 * @Version V1.0
 */

@Slf4j
public class ChatMsgHandler implements MsgHandler {
    @Override
    public void handler(String data, WsRequest wsRequest, ChannelContext channelContext) {

        ChatInMsg chatInMsg = JSONUtil.toBean(data, ChatInMsg.class);

        ImUser mine = chatInMsg.getMine();
        ImTo to = chatInMsg.getTo();


        //消息处理

        ImMsg imMsg = new ImMsg();
        imMsg.setContent(mine.getContent());
        imMsg.setAvatar(mine.getAvatar());
        imMsg.setMine(false); //是否是自己发送的消息

        imMsg.setUsername(mine.getUsername());
        imMsg.setFromid(mine.getId());

        imMsg.setId(Consts.IM_GROUP_ID);
        imMsg.setTimeStamp(new Date());
        imMsg.setType(to.getType());

        ChatOutMsg chatOutMsg = new ChatOutMsg();
        chatOutMsg.setEmit("chatMessage");
        chatOutMsg.setData(imMsg);

        String result = JSONUtil.toJsonStr(chatOutMsg);
        log.info("群聊消息 --------> {}",result);

        WsResponse wsResponse = WsResponse.fromText(result, "utf-8");

        ExcludeMineChannelContextFilter filter = new ExcludeMineChannelContextFilter();
        filter.setCurrentContext(channelContext);

        Tio.sendToGroup(channelContext.getGroupContext(),Consts.IM_GROUP_NAME,wsResponse,filter);




    }
}
