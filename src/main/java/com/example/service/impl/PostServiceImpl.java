package com.example.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bussniess.post.delete.DeleteThread;
import com.example.entity.Post;
import com.example.mapper.PostMapper;
import com.example.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.util.RedisUtil;
import com.example.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ChangQi
 * @since 2022-03-11
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    PostMapper postMapper;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 1分页信息 2分类 3用户 4置顶 5精选 6排序
     *
     * @param page       分页信息
     * @param categoryId 分类
     * @param userId     用户
     * @param level      置顶
     * @param recommend  精选
     * @param order      排序
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     **/
    @Override
    public IPage<PostVo> paging(Page page, Long categoryId, Long userId, Integer level, Boolean recommend, String order) {

        if (level == null) level = -1;

        QueryWrapper wrapper = new QueryWrapper<Post>()
                .eq(categoryId != null, "category_id", categoryId)
                .eq(userId != null, "user_id", userId)
                .eq(level == 0, "level", 0)
                .gt(level > 0, "level", 0)
                .orderByDesc(order != null, order);

        return postMapper.selectPosts(page, wrapper);
    }

    /**
     * 单个信息查询
     *
     * @param wrapper
     * @return com.example.vo.PostVo
     **/
    @Override
    public PostVo selectOnePost(QueryWrapper<Post> wrapper) {
        return postMapper.selectOnePost(wrapper);
    }

    /**
     * 本周热议功能
     *
     * @param
     * @return void
     **/
    @Override
    public void initWeekRank() {
        // 获取7天内的发表文章
        List<Post> posts = this.list(new QueryWrapper<Post>()
                .ge("created", DateUtil.offsetDay(new Date(), -7))
                .select("id, title, user_id, comment_count, view_count, created")
        );

        // 初始化文章的总评论量
        for (Post post : posts) {
            String key = "day:rank:" + DateUtil.format(post.getCreated(), DatePattern.PURE_DATE_FORMAT);

            redisUtil.zSet(key, post.getId(), post.getCommentCount());

            //  七天后自动过期
            long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
            long expireTime = (7 - between) * 24 * 60 * 60;

            redisUtil.expire(key, expireTime);
            //缓存文章的一些基本信息（ID，标题，评论数量，作者）采用hash结构保存数据

            this.hashCachePostIdAndTitle(post, expireTime);


        }
        // 做并集

        this.zunionAndStoreLast7DayForWeekRank();
    }

    @Override
    public void incrCommentCountAndUnionForWeekRank(long postId, boolean isIncr) {
        String currentKey = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        redisUtil.zIncrementScore(currentKey, postId, isIncr ? 1 : -1);

        Post post = this.getById(postId);

        //  七天后自动过期
        long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
        long expireTime = (7 - between) * 24 * 60 * 60;

        //缓存post基本信息
        this.hashCachePostIdAndTitle(post, expireTime);

        // 重新并集
        this.zunionAndStoreLast7DayForWeekRank();


    }


    /**
     * 过去7天合并每日评论并集操作
     * 文章每日评论并集
     *
     * @param
     * @return void
     **/
    private void zunionAndStoreLast7DayForWeekRank() {
        String currentKey = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);

        String destKey = "week:rank";
        List<String> otherKeys = new ArrayList<>();
        for (int i = -6; i < 0; i++) {
            String temp = "day:rank:" + DateUtil.format(DateUtil.offsetDay(new Date(), i), DatePattern.PURE_DATE_FORMAT);
            otherKeys.add(temp);
        }
        redisUtil.zUnionAndStore(currentKey, otherKeys, destKey);


    }

    /**
     * 缓存文章的基本信息
     *
     * @param post
     * @param expireTime
     * @return void
     **/
    private void hashCachePostIdAndTitle(Post post, long expireTime) {
        //判断数据是否存在
        String key = "rank:post:" + post.getId();
        boolean hasKey = redisUtil.hasKey(key);
        if (!hasKey) {
            redisUtil.hset(key, "post:id", post.getId(), expireTime);
            redisUtil.hset(key, "post:title", post.getTitle(), expireTime);
            redisUtil.hset(key, "post:commentCount", post.getCommentCount(), expireTime);
            redisUtil.hset(key, "post:viewCount", post.getViewCount(), expireTime);
        }

    }

    @Override
    public void putViewCount(PostVo vo) {
        String key = "rank:post:" + vo.getId();
        //1.从缓存中获取viewcount
        Integer viewCount = (Integer)redisUtil.hget(key, "post:viewCount");

        //2.如果没有，就从实体中获取
        if(viewCount!=null) {
            vo.setViewCount(viewCount+1);
        } else {
            vo.setViewCount(vo.getViewCount()+1);
        }
        //3.同步到缓存
        redisUtil.hset(key,"post:viewCount",vo.getViewCount());
    }


}
