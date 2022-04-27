package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Chang Qi
 * @date 2022/4/27 20:36
 * @description
 * @Version V1.0
 */
@EnableAsync
@Configuration
@EnableWebSocketMessageBroker //开启使用STOMP协议来传输基于代理Broker的消息，
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    /**
     *  注册Stomp端点
     * addEndpoint：添加STOMP协议的端点。提供WebSocket或SockJS客户端访问的地址
     * withSockJS：使用SockJS协议
     * @param  registry
     * @return void
     *
     **/
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .withSockJS();
    }

   /**
    * *
    * 配置消息代理
    * 启动Broker，消息的发送的地址符合配置的前缀来的消息才发送到这个broker
    *
    * @param registry
    * @return void
    *
    **/
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/use/","/topic"); //推送消息前缀
        registry.setApplicationDestinationPrefixes("/app");

    }


}
