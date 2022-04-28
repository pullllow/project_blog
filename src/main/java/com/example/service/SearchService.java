package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.search.common.PostMqIndexMessage;
import com.example.vo.PostVo;

import java.util.List;

/**
 * @author Chang Qi
 * @date 2022/4/28 10:59
 * @description
 * @Version V1.0
 */

public interface SearchService {
    int initEsData(List<PostVo> records);

    IPage search(Page page, String keyword);

    void createOrUpdate(PostMqIndexMessage message);

    void remove(PostMqIndexMessage message);
}
