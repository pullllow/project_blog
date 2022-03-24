package com.example.vo;
/*
 *  @author changqi
 *  @date 2022/3/21 20:14
 *  @description
 *  @Version V1.0
 */

import com.example.entity.Post;
import lombok.Data;

@Data
public class PostVo extends Post {

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String categoryName;

}
