package com.java2nb.novel.entity;

import lombok.Data;

@Data
public class DesignatedBook {
    private Long id;
    private Integer spiderId;
    private String sourceUrl;
    private String name;
    private Integer status;
    private Integer catId;

}
