package com.java2nb.novel.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PvCount {
    private Long id;
    private String ip;
    private Integer visitNum;
    private Date createTime;
    private Date updateTime;

}
