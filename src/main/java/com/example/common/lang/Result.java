package com.example.common.lang;
/*
 *  @author changqi
 *  @date 2022/3/30 21:15
 *  @description
 *  @Version V1.0
 */


import lombok.Data;

import java.io.Serializable;



@Data
public class Result implements Serializable {

    // 0 成功 -1 失败
    private int status;
    private String msg;
    private Object data;
    private String action;

    public static Result success() {
        return Result.success("操作成功", null);
    }

    public static Result success(Object data) {
        return Result.success("操作成功", data);
    }

    public static Result success(String msg, Object data) {
        Result result = new Result();
        result.status = 0;
        result.msg = msg;
        result.data = data;
        return result;

    }

    public static Result fail(String msg) {
        Result result = new Result();
        result.status = -1;
        result.msg = msg;
        result.data = null;
        return result;

    }

    public Result action(String action) {
        this.action  = action;
        return this;
    }



}
