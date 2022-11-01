package com.java2nb.novel.service.impl;

import com.java2nb.novel.mapper.AdverMapper;
import com.java2nb.novel.service.AdverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Service
public class AdverServiceImpl implements AdverService {
    @Autowired
    AdverMapper mapper;


    /**
     * 判断该ip是否加载广告
     * @param request
     * @return 1  加载   2  不加载
     */
    @Override
    public int isShowAdver(HttpServletRequest request) {
        // 判断该ip是否不加载广告
        String ip = null;
        String realIp = request.getHeader("X-Real-IP");
        String xFordFor = request.getHeader("X-Forwarded-For");
        if (realIp != null) {
            ip = realIp;
        } else if (xFordFor != null) {
            ip = xFordFor;
        }else {
            ip = "127.0.0.1";
        }


        if (ip != null) {
            List<String> i = mapper.selectIp(ip);
            if (i.size() != 0) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }


    /**
     * 对当天点击过广告的ip进行记录
     * @param request
     */
    @Override
    public void clickRecode(HttpServletRequest request) {
        String ip = null;
        String realIp = request.getHeader("X-Real-IP");
        String xFordFor = request.getHeader("X-Forwarded-For");
        if (realIp != null) {
            ip = realIp;
        } else if (xFordFor != null) {
            ip = xFordFor;
        } else {
            ip = "127.0.0.1";
        }

        if (ip != null) {
            List<String> i = mapper.selectIp(ip);
            if (i.size() == 0) {
                // 如果不存在,则插入
                mapper.insert(ip);
            }
        }
    }
}
