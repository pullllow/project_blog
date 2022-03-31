package com.example.shiro;
/*
 *  @author changqi
 *  @date 2022/3/31 13:24
 *  @description
 *  @Version V1.0
 */

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AccountProfile implements Serializable {

    private Long id;
    private String username;
    private String email;

    private String avatar;
    private String sign;

    private Date created;


}
