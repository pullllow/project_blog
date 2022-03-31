package com.example.schedules;
/*
 *  @author changqi
 *  @date 2022/3/26 21:02
 *  @description 将缓存数据定时刷新到MySQL
 *  @Version V1.0
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.Post;
import com.example.service.PostService;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ViewCountSyncTask {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    PostService postService;

    @Scheduled(cron = "0/5 * * * * *")
    public void task() {
        Set<String> keys = redisTemplate.keys("rank:post:*");
        //String postId = key.substring("rank:post:".length());
        List<String> ids = new ArrayList<>();
        for (String key : keys) {
            if (redisUtil.hHasKey(key, "post:viewCount")) {
                ids.add(key.substring("rank:post:".length()));
            }
        }
        if (ids.isEmpty()) return;

        //需要更新阅读量
        List<Post> posts = postService.list(new QueryWrapper<Post>()
                .in("id", ids));

        posts.stream().forEach((post -> {
            Integer viewCount = (Integer) redisUtil.hget("rank:post:" + post.getId(), "post:viewCount");
            post.setViewCount(viewCount);
        }));

        if (posts.isEmpty()) return;

        boolean isSucc = postService.updateBatchById(posts);

        if (isSucc) {
            ids.stream().forEach((id) -> {
                redisUtil.hdel("rank:post:" + id, "post:viewCount");
                System.out.println("rank:post:" + id + "------------>同步成功");
            });
        }

    }
}
