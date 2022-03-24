package com.example.controller;
/*
 *  @author changqi
 *  @date 2022/3/12 9:42
 *  @description
 *  @Version V1.0
 */


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Post;
import com.example.vo.PostVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController extends BaseController {

    @RequestMapping({"", "/", "/index"})
    public String index() {

        //当前页码
        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        //当前页 数据数量
        int size = ServletRequestUtils.getIntParameter(req, "size", 1);
        Page page = new Page(pn, size);

        //1分页信息 2分类 3用户 4置顶 5精选 6排序
        IPage results = postService.paging(page, null, null, null, null, "created");

        req.setAttribute("pageData", results);
        req.setAttribute("currentCategoryId", 0);
        return "index";
    }

}
