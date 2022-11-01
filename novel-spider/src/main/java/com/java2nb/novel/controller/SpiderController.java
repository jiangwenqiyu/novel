package com.java2nb.novel.controller;

import com.java2nb.novel.service.BookService;
import io.github.xxyopen.model.resp.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("spider")
public class SpiderController {

//    @Autowired
//    BookService bookService;
//
//
//    /**
//     * 新书入库
//     * @return
//     */
//    @GetMapping("new")
//    @ResponseBody
//    public RestResult newBook(@RequestParam("id") String id) throws IOException {
//        return bookService.newBookSpider(id);
//    }
//
//
//
//    /**
//     * 旧书更新,根据book表的spider_detail,获取章节信息和库中的对比
//     * @return
//     */
//    @GetMapping("update")
//    @ResponseBody
//    public RestResult updateBook() throws IOException {
//        return bookService.updateBook();
//    }


}
