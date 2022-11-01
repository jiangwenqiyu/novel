package com.java2nb.novel.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RuleBean {

    //    需要翻页的表达式{catId}、{page}、{catMap}    http://www.xxx.com/玄幻小说/第一页/玄幻小说id1.html
    private String bookListUrl;

    //    类目映射
    private Map<String, String> catId;

    //    总页数表达式, 如果不需要翻页，直接填 1
    private String totalPageExp;

    // 详情页表达式
    private String detailExp;

    //    书名表达式
    private String bookNameExp;

    //    作者表达式
    private String authorExp;

    //    说明表达式
    private String descExp;

    //    图片表达式
    private String imgExp;


    //    内容表达式
    private String contentStartExp;
    private String contentEndExp;

    // 内容正则, 获取内容的时候，先判断这个，如果有内容，则按正则匹配，如果没有，则按首位截取
    private String contentRegex = "";

    // 拼接详情页的前缀
    private String foreUrl;

    // 完结表达式：如果能查到，就是完结，否则连载中
    private String statusExp;

    // 图片前缀
    private String imgForeUrl;

    // 章节列表
    private String chapterListExp;

    // 章节内容链接
    private String chapterUrlExp;

    // 内容页面链接前缀
    private String chapterUrlFore;

    // 内容信息需要替换的内容
    private String contentclean;

    // 请求头
    private Map<String, String> headers = new HashMap<>();

    // 网站编码格式
    private String encoding = "utf-8";

    // 内容url前置正则表达式，若为""，则不查找
    private String chapterUrlForeReg;
}
