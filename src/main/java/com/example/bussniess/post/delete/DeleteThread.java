package com.example.bussniess.post.delete;

import cn.hutool.core.map.MapUtil;
import com.example.common.lang.Result;
import com.example.config.ApplicationBeanFactory;
import com.example.config.RabbitMqConfig;
import com.example.entity.Post;
import com.example.search.common.PostMqIndexMessage;
import com.example.service.PostService;
import com.example.service.UserCollectionService;
import com.example.service.UserMessageService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

/**
 * @author Chang Qi
 * @date 2022/8/30 21:03
 * @description 删除博客线程
 * @Version V1.0
 */


public class DeleteThread implements Callable<Result> {

    public long id;

    public long profileId;


    private PostService postService;


    private UserMessageService userMessageService;


    private UserCollectionService userCollectionService;


    private AmqpTemplate amqpTemplate;


    public DeleteThread(long id, long profileId) {
        this.id = id;
        this.profileId = profileId;

        postService = ApplicationBeanFactory.getBean(PostService.class);
        userMessageService = ApplicationBeanFactory.getBean(UserMessageService.class);
        userCollectionService = ApplicationBeanFactory.getBean(UserCollectionService.class);
        amqpTemplate = ApplicationBeanFactory.getBean(AmqpTemplate.class);


    }

    @Override
    public Result call() throws Exception {


        Post post = postService.getById(id);

        Result result = null;

        if(post==null) {
            return  Result.fail("该帖子已经被删除");
        }
        if(post.getUserId().longValue() != profileId) {
            return  Result.fail("无权限删除此文章");
        }

        //boolean status = false;
        try {
            if(postService.removeById(id)) {
                result = Result.success("删除成功");

                //删除相关消息、收藏等
                if(!userMessageService.removeByMap(MapUtil.of("post_id", id))) {
                    result = Result.fail("用户消息删除失败");
                    return result;
                }


                if(!userCollectionService.removeByMap(MapUtil.of("post_id", id))) {
                    result = Result.fail("用户收藏删除失败");
                    return result;
                }

                // 通知消息给mq，告知删除
                amqpTemplate.convertAndSend(RabbitMqConfig.ES_EXCHANGE, RabbitMqConfig.ES_BIND_KEY,
                        new PostMqIndexMessage(post.getId(), PostMqIndexMessage.REMOVE));

            } else {
                result = Result.fail("删除失败");
                return result;
            }
        } catch (AmqpException e) {
            e.printStackTrace();
        } finally {
            return result;
        }


    }
}
