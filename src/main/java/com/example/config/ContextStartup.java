package com.example.config;
/*
 *  @author changqi
 *  @date 2022/3/12 20:44
 *  @description
 *  @Version V1.0
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.Category;
import com.example.service.CategoryService;
import com.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

@Component
public class ContextStartup implements ApplicationRunner, ServletContextAware {

    @Autowired
    CategoryService categoryService;

    ServletContext servletContext;

    @Autowired
    PostService postService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        //项目启动时调用
        //将分类标签注入
        List<Category> categories = categoryService.list(new QueryWrapper<Category>()
                .eq("status", 0)
        );
        servletContext.setAttribute("categories",categories);

        //本周热议功能
        postService.initWeekRank();




    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        //应用级别注入
        servletContext.setAttribute("base", servletContext.getContextPath());
        this.servletContext = servletContext;
    }
}
