package com.example.config;

import cn.hutool.core.map.MapUtil;
import com.example.shiro.AccountRealm;
import com.example.shiro.AuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;


/*
 *  @author changqi
 *  @date 2022/3/31 13:42
 *  @description
 *  @Version V1.0
 */

@Slf4j
@Configuration
public class ShiroConfig {

    @Bean
    public SecurityManager securityManager(AccountRealm accountRealm) {

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(accountRealm);

        log.info("--------->securityManager注入成功");
        return securityManager;

    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {

        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        filterFactoryBean.setSecurityManager(securityManager);
        // 配置登录的url和登录成功的url
        filterFactoryBean.setLoginUrl("/login");
        filterFactoryBean.setSuccessUrl("/user/center");
        // 配置未授权跳转页面
        filterFactoryBean.setUnauthorizedUrl("/error/403");

        filterFactoryBean.setFilters(MapUtil.of("auth", authFilter()));


        Map<String, String> hashMap = new LinkedHashMap<>();
        //hashMap.put("/user/home","authc");
        hashMap.put("/user/set", "auth");
        hashMap.put("/user/upload", "auth");
        hashMap.put("/user/index", "auth");
        hashMap.put("/user/collection", "auth");
        hashMap.put("/user/mess", "auth");

        hashMap.put("/mess/remove", "auth");


        hashMap.put("/post/edit", "auth");
        hashMap.put("/post/submit", "auth");
        hashMap.put("/post/delete", "auth");
        hashMap.put("/post/reply/", "auth");

        hashMap.put("/collection/find", "auth");
        hashMap.put("/collection/add", "auth");
        hashMap.put("/collection/remove", "auth");

        hashMap.put("/login", "anon");


        filterFactoryBean.setFilterChainDefinitionMap(hashMap);

        return filterFactoryBean;

    }


    @Bean
    public AuthFilter authFilter() {
        return new AuthFilter();
    }
}
