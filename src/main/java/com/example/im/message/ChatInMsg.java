package com.example.im.message;

import com.example.im.vo.ImTo;
import com.example.im.vo.ImUser;
import lombok.Data;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:13
 * @description
 * @Version V1.0
 */

@Data
public class ChatInMsg {

    private ImUser mine;
    private ImTo to;
}
