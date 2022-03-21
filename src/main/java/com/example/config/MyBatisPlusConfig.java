package com.example.config;
/*
 *  @author changqi
 *  @date 2022/3/12 9:31
 *  @description
 *  @Version V1.0
 */


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.mapper")
public class MyBatisPlusConfig {

}
