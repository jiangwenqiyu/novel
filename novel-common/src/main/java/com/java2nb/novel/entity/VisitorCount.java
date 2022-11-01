package com.java2nb.novel.entity;

import lombok.Data;

import java.util.Date;

@Data
public class VisitorCount {
    private Long id;
    private String ip;
    private Date visitTime;
}
