package com.example.im.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:15
 * @description
 * @Version V1.0
 */

@Data
public class ImMsg {

    private String username;
    private String avatar;

    private String type; //聊天窗口来源类型，从发送消息传递的to中获取
    private String content;

    private Long cid;
    private Boolean mine;
    private Long fromid;

    private Date timeStamp;

    private Long id; //消息的来源ID(如果是私聊，则是用户id，如果是群聊，则是群组id)


}
