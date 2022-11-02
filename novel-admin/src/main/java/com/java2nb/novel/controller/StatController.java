package com.java2nb.novel.controller;

import com.java2nb.common.utils.DateUtils;
import com.java2nb.common.utils.R;
import com.java2nb.novel.service.AuthorService;
import com.java2nb.novel.service.BookService;
import com.java2nb.novel.service.PayService;
import com.java2nb.novel.service.UserService;
import com.java2nb.system.dao.CountMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计
 *
 * @author xiongxy
 * @email 1179705413@qq.com
 * @date 2020-12-01 03:40:12
 */

@Controller
@RequestMapping("/novel/stat")
public class StatController {
    @Autowired
    private UserService userService;

    @Autowired
    private CountMapper countMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PayService orderPayService;


    @ResponseBody
    @GetMapping("/countSta")
    public R countUser() {
        Map map = new HashMap<>(0);
        int userCount = userService.count(map);
        int bookCount = countMapper.bookCount();
        int todayCount = countMapper.todayCount();
        Long pvCount = countMapper.pvCount();
        int feedBack = countMapper.feedBack();

        return R.ok().put("userCount",userCount)
                .put("bookCount",bookCount)
                .put("todayCount",todayCount)
                .put("pvCount",pvCount)
                .put("feedBack",feedBack);
    }

    @ResponseBody
    @GetMapping("/tableSta")
    @SneakyThrows
    public R tableSta() {
        Date currentDate = new Date();
        List<String> dateList = DateUtils.getDateList(7,currentDate);
        Date minDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateList.get(0));
        Map<Object,Object> userTableSta = userService.tableSta(minDate);
        Map<Object,Object> bookTableSta = bookService.tableSta(minDate);
//        Map<Object,Object> authorTableSta = authorService.tableSta(minDate);
//        Map<Object,Object> orderTableSta = orderPayService.tableSta(minDate);
        List<Map<Object,Object>> ipmaps = countMapper.ipSta(minDate);
        Map<Object,Object> ipSta = ipmaps.stream().collect(Collectors.toMap(x -> x.get("staDate"), x -> x.get("ip")));
        List<Map<Object,Object>> pvmaps = countMapper.pvSta(minDate);
        Map<Object,Object> pvSta = pvmaps.stream().collect(Collectors.toMap(x -> x.get("staDate"), x -> x.get("ip")));

        return R.ok().put("dateList",dateList)
                .put("userTableSta",userTableSta)
                .put("bookTableSta",bookTableSta)
                .put("ipSta",ipSta)
                .put("pvSta",pvSta)
                ;
    }


    @ResponseBody
    @GetMapping("/deploy")
    @SneakyThrows
    public R deploy(@RequestParam String type) {
        // type 1 部署前端   2 部署后端   3  部署爬虫
        String cmd = "";
        if (type.equals("1")) {
            cmd = "cd /data && ./front.sh";
        } else if (type.equals("2")) {
            cmd = "cd /data && ./admin.sh";
        } else {
            cmd = "cd /data && ./spider.sh";
        }
        Process process = Runtime.getRuntime().exec(cmd);
        String ret = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((ret = br.readLine())  != null) {
            System.out.println(ret);
        }



        return new R();
    }




}
