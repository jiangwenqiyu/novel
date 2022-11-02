package com.java2nb.novel.service;

import com.java2nb.novel.entity.Advertisment;

import javax.servlet.http.HttpServletRequest;

public interface AdverService {

    int isShowAdver(HttpServletRequest request, String id);

    void clickRecode(HttpServletRequest request, String id);


    Advertisment selectAdver(HttpServletRequest request);
}
