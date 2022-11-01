package com.java2nb.novel.vo;

import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.entity.BookIndex;
import lombok.Data;

import java.io.Serializable;

@Data
public class BookIndexVo extends BookIndex implements Serializable {
    private BookContent bookContent;


}
