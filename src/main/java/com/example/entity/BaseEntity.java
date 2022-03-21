package com.example.entity;
/*
 *  @author changqi
 *  @date 2022/3/11 22:03
 *  @description
 *  @Version V1.0
 */

import lombok.Data;
import java.util.Date;

@Data
public class BaseEntity {
    private Long id;
    private Date created;
    private Date modified;
}
