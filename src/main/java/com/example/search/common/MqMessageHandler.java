package com.example.search.common;

import com.example.config.RabbitMqConfig;
import com.example.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chang Qi
 * @date 2022/4/28 16:31
 * @description
 * @Version V1.0
 */

@Slf4j
@Component
@RabbitListener(queues = RabbitMqConfig.ES_QUEUE)
public class MqMessageHandler {

    @Autowired
    SearchService searchService;

    @RabbitHandler
    public void handler(PostMqIndexMessage message) {
        log.info("MQ 收到一条消息： {}",  message.toString());

        switch (message.getType()) {
            case PostMqIndexMessage.CREATE_OR_UPDATE:
                searchService.createOrUpdate(message);
                break;
            case PostMqIndexMessage.REMOVE:
                searchService.remove(message);
                break;
            default:
                log.error("没找到对应的消息类型，请注意！ —--》{}" ,message.toString());
                break;
        }

    }

}
