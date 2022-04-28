package com.example.search.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Chang Qi
 * @date 2022/4/28 16:30
 * @description
 * @Version V1.0
 */
@Data
@AllArgsConstructor
public class PostMqIndexMessage implements Serializable {

    //Type
    public final static String CREATE_OR_UPDATE = "create_update";
    public final static String REMOVE = "remove";

    private Long postId;
    private String type;

}
