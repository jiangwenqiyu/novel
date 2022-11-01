package com.java2nb.novel.vo;

import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SpiderVo extends Book implements Serializable {

    List<BookIndexVo> bookIndexList;


}
