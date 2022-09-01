package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Chang Qi
 * @date 2022/8/30 21:10
 * @description @EnableAsync来开启异步
 * @Version V1.0
 */

@Configuration
@EnableAsync
public class ThreadPoolConfig {


    //创建线程池，用于删除博客
    @Bean(name = "deleteThreadPool")
    public ThreadPoolTaskExecutor deleteThreadPool() {
        return getThreadPool();
    }

    // 创建线程池，用于处理评论发表
    @Bean(name = "replyThreadPool")
    public ThreadPoolTaskExecutor replyThreadPool() {
        return getThreadPool();
    }

    public ThreadPoolTaskExecutor getThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数： 线程创建时候初始化的线程数
        executor.setCorePoolSize(10);
        // 最大线程数，线程池最大的线程数，只有缓存队列满之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(20);
        // 阻塞队列： 用来阻塞执行任务的队列
        executor.setQueueCapacity(100);
        // 允许线程空闲时间60s: 当超过核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀：定位处理任务所在线程池
        executor.setThreadNamePrefix("threadPool-");
        // 阻塞队列满的拒绝策略，由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}
