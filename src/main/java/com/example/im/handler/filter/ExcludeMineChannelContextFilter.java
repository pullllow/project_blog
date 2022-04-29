package com.example.im.handler.filter;

import lombok.Data;
import org.tio.core.ChannelContext;
import org.tio.core.ChannelContextFilter;

/**
 * @author Chang Qi
 * @date 2022/4/29 16:16
 * @description
 * @Version V1.0
 */

@Data
public class ExcludeMineChannelContextFilter implements ChannelContextFilter {

    private ChannelContext currentContext;

    @Override
    public boolean filter(ChannelContext channelContext) {

        //过滤当前用户，不需要发送消息
        if(currentContext.userid.equals(channelContext.userid)) {
            return false;
        }

        return true;
    }
}
