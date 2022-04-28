package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Post;
import com.example.search.common.PostMqIndexMessage;
import com.example.search.model.PostDocument;
import com.example.search.repository.PostRepository;
import com.example.service.PostService;
import com.example.service.SearchService;
import com.example.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchPhase;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chang Qi
 * @date 2022/4/28 11:04
 * @description
 * @Version V1.0
 */

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PostService postService;

    @Override
    public int initEsData(List<PostVo> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }

        List<PostDocument> documents = new ArrayList<>();
        for(PostVo vo : records) {
            //映射转换
            PostDocument postDocument = modelMapper.map(vo, PostDocument.class);
            documents.add(postDocument);
        }

        postRepository.saveAll(documents);
        return documents.size();
    }

    @Override
    public IPage search(Page page, String keyword) {

        //分页信息 mybatis plus 的 page 转成 jpa 的 page
        Long current = page.getCurrent() - 1;
        Long size = page.getSize();
        Pageable pageable = PageRequest.of(current.intValue(), size.intValue());

        //搜索es得到pageData
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword,
                "title", "authorName", "categoryName");
        org.springframework.data.domain.Page<PostDocument> documents = postRepository.search(multiMatchQueryBuilder, pageable);

        //结果信息 jpa 的pageData 转为 mybatis plus 的pageData
        IPage pageData = new Page(page.getCurrent(), page.getSize(), documents.getTotalElements());
        pageData.setRecords(documents.getContent());

        return pageData;

    }

    @Override
    public void createOrUpdate(PostMqIndexMessage message) {

        Long postId = message.getPostId();

        PostVo postVo = postService.selectOnePost(new QueryWrapper<Post>()
                .eq("p.id", postId)
        );

        PostDocument postDocument = modelMapper.map(postVo, PostDocument.class);

        postRepository.save(postDocument);

        log.info("ES 索引更新成功！ ---》 {}", postDocument.toString());


    }

    @Override
    public void remove(PostMqIndexMessage message) {
        Long postId = message.getPostId();

        postRepository.deleteById(postId);

        log.info("ES 索引删除成功！ ---》 {}", message.toString());


    }
}
