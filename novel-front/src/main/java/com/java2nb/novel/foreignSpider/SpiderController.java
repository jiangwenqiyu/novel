package com.java2nb.novel.foreignSpider;

import com.java2nb.novel.service.SpiderService;
import com.java2nb.novel.vo.SpiderVo;
import io.github.xxyopen.model.resp.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("test")
public class SpiderController extends ExController{
    @Autowired
    SpiderService spiderService;


    @GetMapping("time")
    @ResponseBody
    public Map result() throws Exception {
        Map m = new HashMap();
        m.put("javatime", new Date());

        return m;
    }




}
