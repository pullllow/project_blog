package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.UserMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vo.UserMessageVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ChangQi
 * @since 2022-03-11
 */

public interface UserMessageService extends IService<UserMessage> {

    IPage paging(Page page, QueryWrapper<UserMessage> wrapper);

    void updateToRead(List<Long> ids);
}
