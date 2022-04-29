package com.example.im.message;

import com.example.im.vo.ImMsg;
import lombok.Data;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:14
 * @description
 * @Version V1.0
 */

@Data
public class ChatOutMsg {

    private String emit;
    private ImMsg data;
}
