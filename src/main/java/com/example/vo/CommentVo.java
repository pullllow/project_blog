package com.example.vo;
/*
 *  @author changqi
 *  @date 2022/3/25 20:11
 *  @description
 *  @Version V1.0
 */

import com.example.entity.Comment;
import lombok.Data;

@Data
public class CommentVo extends Comment {

    private Long authorId;
    private String authorName;
    private String authorAvatar;



}
