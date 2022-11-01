package com.java2nb.novel.service;

import io.github.xxyopen.model.resp.RestResult;

import java.io.IOException;


public interface BookService {

    RestResult newBookSpider(String id) throws IOException;


    RestResult updateBook();
}