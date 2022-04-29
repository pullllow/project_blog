package com.example.service;

import com.example.im.vo.ImMsg;
import com.example.im.vo.ImUser;

import java.util.List;

/**
 * @author Chang Qi
 * @date 2022/4/28 20:50
 * @description
 * @Version V1.0
 */

public interface ChatService {
    ImUser getCurrentUser();

    void setGroupHistoryMsg(ImMsg imMsg);

    List<Object> getGroupHistoryMsg(int count);
}
