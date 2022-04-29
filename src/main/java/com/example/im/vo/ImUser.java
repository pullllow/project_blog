package com.example.im.vo;

import lombok.Data;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:24
 * @description
 * @Version V1.0
 */

@Data
public class ImUser {

    public final static String ONLINE_STATUS = "online";
    public final static String HIDE_STATUS = "hide";

    private Long id;
    private String username;
    private String status; // 在线状态 online： 在线 hide: 隐身
    private String sign; //我的签名
    private String avatar; //我的头像

    private Boolean mine; //是否自己发送的消息
    private String content; //消息内容

}
