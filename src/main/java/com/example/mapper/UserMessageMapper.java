package com.example.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.UserMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vo.UserMessageVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ChangQi
 * @since 2022-03-11
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    IPage<UserMessageVo> selectMessages(Page page, @Param(Constants.WRAPPER) QueryWrapper<UserMessage> wrapper);


    void updateToRead(@Param(Constants.WRAPPER)QueryWrapper<UserMessage> id);
}
