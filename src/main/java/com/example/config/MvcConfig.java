package com.example.config;
/*
 *  @author changqi
 *  @date 2022/3/31 20:04
 *  @description
 *  @Version V1.0
 */

import com.example.common.lang.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    Consts consts;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/avatar/**")
                .addResourceLocations("file:///" + consts.getUploadDir() + "/avatar/");


        //WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
