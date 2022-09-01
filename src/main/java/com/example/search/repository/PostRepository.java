package com.example.search.repository;

import com.example.search.model.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Chang Qi
 * @date 2022/4/28 10:40
 * @description 符合jpa命名规范的接口 实现ElasticsearchRepository
 * @Version V1.0
 */

@Repository
public interface PostRepository extends ElasticsearchRepository<PostDocument, Long> {



}
