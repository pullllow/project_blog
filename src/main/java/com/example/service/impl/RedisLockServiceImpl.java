package com.example.service.impl;

import com.example.service.RedisLockService;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Chang Qi
 * @date 2022/8/31 21:42
 * @description 分布式锁框架
 * @Version V1.0
 */

@Service
public class RedisLockServiceImpl implements RedisLockService{


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    public static final String unLockScript = "if redis.call(\"get\", KEYS[1]) == ARGV[1]\n"
                                            + "then\n"
                                            + "     return redis.call(\"del\", KEYS[1])\n"
                                            + "else\n"
                                            + "     return 0\n"
                                            + "end";


    /**
     * 加锁，有阻塞
     *
     * @param name
     * @param expire 单位s
     * @param timeout 单位ms
     * @return java.lang.String
     *
     **/
    @Override
    public String lock(String name, long expire, long timeout) {
        long startTime = System.currentTimeMillis();
        String token;

        do {
            token = tryLock(name, expire);
            if(token==null) {
                // 设置等待时间，若等待时间过长则获取锁失败
                if((System.currentTimeMillis() - startTime) > (timeout - 50)) {
                    break;
                }
                try {
                    Thread.sleep(50); // 每50ms尝试一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } while (token == null);

        return token;
    }

    /**
     *  释放锁
     *
     * @param name
     * @param token
     * @return java.lang.Boolean
     *
     **/
    @Override
    public Boolean unlock(String name, String token) {
        byte[][] keyArgs = new byte[2][];
        keyArgs[0] = name.getBytes(StandardCharsets.UTF_8);
        keyArgs[1] = token.getBytes(StandardCharsets.UTF_8);

        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();

        try {
            Long result = connection.scriptingCommands()
                    .eval(unLockScript.getBytes(StandardCharsets.UTF_8), ReturnType.INTEGER, 1, keyArgs);
            if(result!=null && result>0) {
                return true;
            }
        } finally {
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }

        return false;
    }


    /**
     * 加锁， 有阻塞
     *
     * @param name
     * @param expire
     * @return java.lang.String
     *
     **/
    @Override
    public String tryLock(String name, long expire) {
        String token = UUID.randomUUID().toString();
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();

        try {
            Boolean result = connection.set(name.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8),
                    Expiration.from(expire, TimeUnit.SECONDS), RedisStringCommands.SetOption.SET_IF_ABSENT);
            if(result != null && result) {
                return token;
            }
        } finally {
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
        return null;
    }
}
