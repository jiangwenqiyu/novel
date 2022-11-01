package com.java2nb.novel.service;

import javax.servlet.http.HttpServletRequest;

public interface AdverService {

    public int isShowAdver(HttpServletRequest request);

    public void clickRecode(HttpServletRequest request);
}
