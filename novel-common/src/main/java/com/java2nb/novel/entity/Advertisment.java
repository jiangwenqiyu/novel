package com.java2nb.novel.entity;

import lombok.Data;

@Data
public class Advertisment {
    private Integer id;
    private String name;
    private String source;
    private String sourceUrl;
    private String adverCode;
    private String autoClick;
    private Integer status;

}
