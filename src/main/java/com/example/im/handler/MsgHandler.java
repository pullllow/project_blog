package com.example.im.handler;

import org.tio.core.ChannelContext;
import org.tio.websocket.common.WsRequest;

/**
 * @author Chang Qi
 * @date 2022/4/28 19:59
 * @description
 * @Version V1.0
 */

public interface MsgHandler {
    void handler(String data, WsRequest wsRequest, ChannelContext channelContext);
}
