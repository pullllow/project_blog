package com.example.im.server;

import lombok.extern.slf4j.Slf4j;
import org.tio.server.ServerGroupContext;
import org.tio.websocket.server.WsServerStarter;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.io.IOException;

/**
 * @author Chang Qi
 * @date 2022/4/28 19:38
 * @description
 * @Version V1.0
 */

@Slf4j
public class ImServerStarter {

    private WsServerStarter starter;

    public ImServerStarter(int port) throws IOException {

        IWsMsgHandler handler = new ImWsMsgHandler();

        starter = new WsServerStarter(port, handler);

        ServerGroupContext serverGroupContext = starter.getServerGroupContext();
        serverGroupContext.setHeartbeatTimeout(50000); //设置心跳时间


    }

    public void start() throws IOException {
        starter.start();
        log.info("t-io server start!!");

    }
}
