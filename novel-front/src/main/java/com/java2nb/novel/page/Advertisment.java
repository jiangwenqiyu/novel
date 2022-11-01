package com.java2nb.novel.page;

import com.alibaba.fastjson.JSONObject;
import com.java2nb.novel.mapper.AdverMapper;
import com.java2nb.novel.service.AdverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class Advertisment {

    @Autowired
    AdverMapper mapper;

    @Autowired
    AdverService adverService;




    /**
     * 点击一次广告后，进行记录
     */
    @GetMapping("record")
    @ResponseBody
    public JSONObject adverRecord(HttpServletRequest request) {
        adverService.clickRecode(request);

        return new JSONObject();
    }


    /**
     * js用的，在前端也需判断，是否已经点过广告了，如果点过，就不再加载了
     * @param request
     * @return
     */
    @GetMapping("isShowAdver")
    @ResponseBody
    public Map<String, Integer> isShowAdver(HttpServletRequest request) {
        Map<String, Integer> map = new HashMap<>();
        map.put("isShowAdver", adverService.isShowAdver(request));
        return map;
    }



}
