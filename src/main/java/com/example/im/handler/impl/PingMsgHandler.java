package com.example.im.handler.impl;

import com.example.im.handler.MsgHandler;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.websocket.common.WsRequest;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:04
 * @description
 * @Version V1.0
 */

@Slf4j
public class PingMsgHandler implements MsgHandler {
    @Override
    public void handler(String data, WsRequest wsRequest, ChannelContext channelContext) {
        System.out.println("ping");
    }
}
