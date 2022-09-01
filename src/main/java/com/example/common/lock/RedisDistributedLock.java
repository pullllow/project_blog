package com.example.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Chang Qi
 * @date 2022/9/1 9:58
 * @description
 * @Version V1.0
 */


@Component
@Slf4j
public class RedisDistributedLock {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_SUCCESS = "ok";

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;

    public static final String UNLOCK_LUA;
    public static final String EXLOCK_LUA;
    static {
        //if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("if redis.call('get', KEYS[1]) == ARGV[1]");
        stringBuilder.append("then ");
        stringBuilder.append("    return redis.call('del', KEYS[1])");
        stringBuilder.append("else ");
        stringBuilder.append("    return 0 end");
        UNLOCK_LUA = stringBuilder.toString();
    }

    static {
        //if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1],ARGV[2]) else return '0' end
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("if redis.call('get', KEYS[1]) == ARGV[1] ");
        stringBuilder.append("then ");
        stringBuilder.append("    return redis.call('expire', KEYS[1],ARGV[2])");
        stringBuilder.append("else ");
        stringBuilder.append("    return '0' end");
        EXLOCK_LUA = stringBuilder.toString();
    }





}
