package com.example.im.vo;

import lombok.Data;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:21
 * @description
 * @Version V1.0
 */

@Data
public class ImTo {

    private Long id;
    private String username;
    private String type;
    private String avatar;
    private Integer members;

}
