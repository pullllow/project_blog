package com.example.controller;
/*
 *  @author changqi
 *  @date 2022/3/12 9:42
 *  @description
 *  @Version V1.0
 */


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController extends BaseController {

    @RequestMapping({"", "/", "/index"})
    public String index() {
        req.setAttribute("currentCategoryId",0);
        return "index";
    }

}
