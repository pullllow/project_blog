package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import com.example.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ChangQi
 * @since 2022-03-11
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    CommentMapper commentMapper;

    /**
     * // 1分页 2文章id 3用户id 4排序
     * @param page
     * @param postId
     * @param userId
     * @param order
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.example.vo.CommentVo>
     **/
    @Override
    public IPage<CommentVo> paging(Page page, Long postId, Long userId, String order) {
        return commentMapper.selectComments(page,new QueryWrapper<Comment>()
                .eq(postId!=null,"post_id",postId)
                .eq(userId!=null, "user_id",userId)
                .orderByDesc(order!=null,order));
    }
}
