package com.example.service;

/**
 * @author Chang Qi
 * @date 2022/8/31 21:40
 * @description
 * @Version V1.0
 */

public interface RedisLockService {


    String lock(String name, long expire, long timeout);

    Boolean unlock(String name, String token);

    String tryLock(String name, long expire);
}
