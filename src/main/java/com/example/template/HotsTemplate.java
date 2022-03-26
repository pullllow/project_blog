package com.example.template;
/*
 *  @author changqi
 *  @date 2022/3/26 20:11
 *  @description 本周热议
 *  @Version V1.0
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.templates.DirectiveHandler;
import com.example.common.templates.TemplateDirective;
import com.example.service.PostService;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HotsTemplate extends TemplateDirective {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String getName() {
        return "hots";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {
        String weekRankKey = "week:rank";

        Set<ZSetOperations.TypedTuple> typedTuples = redisUtil.getZSetRank(weekRankKey, 0, 6);

        List<Map> hotPosts = new ArrayList<>();

        for (ZSetOperations.TypedTuple typedTuple : typedTuples) {
            Map<String, Object> map = new HashMap<>();

            Object value = typedTuple.getValue(); //post id
            String postKey = "rank:post:" +value;

            map.put("id", value);
            map.put("title",redisUtil.hget(postKey,"post:title"));
            map.put("commentCount",typedTuple.getScore());

            hotPosts.add(map);
        }
        handler.put(RESULTS,hotPosts).render();

    }
}
