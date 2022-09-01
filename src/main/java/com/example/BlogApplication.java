package com.example;

import com.example.config.ApplicationBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(ApplicationBeanFactory.class)
public class BlogApplication {

    public static void main(String[] args) {

        // 解决elasticsearch启动保存问题
        System.setProperty("es.set.netty.runtime.available.processors", "false");


        SpringApplication.run(BlogApplication.class, args);
        System.out.println("http://localhost:8080");

    }

}
