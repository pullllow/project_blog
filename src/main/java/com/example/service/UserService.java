package com.example.service;

import com.example.common.lang.Result;
import com.example.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shiro.AccountProfile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ChangQi
 * @since 2022-03-11
 */
public interface UserService extends IService<User> {

    Result register(User user);

    AccountProfile login(String email, String password);
}
