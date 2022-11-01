package com.java2nb.novel.core.interceptor;

import com.java2nb.novel.entity.PvCount;
import com.java2nb.novel.entity.VisitorCount;
import com.java2nb.novel.mapper.VisitorCountMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class VisitorsInterceptor implements HandlerInterceptor {

    @Autowired
    VisitorCountMapper mapper;

    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     * 基于请求次数，甄别是否爬虫
     */
    private boolean checkSpider(String ip) {
        if (redisTemplate.hasKey("forbid_" + ip)) {
            redisTemplate.delete(ip);
            return false;
        }

        if (redisTemplate.hasKey(ip)) {
            // ip存在，请求次数+1
            int times = Integer.valueOf(redisTemplate.opsForValue().get(ip));
            times++;
            Long expTime = redisTemplate.getExpire(ip, TimeUnit.SECONDS);
            if (expTime != 0) {
                redisTemplate.opsForValue().set(ip, String.valueOf(times), expTime, TimeUnit.SECONDS);
            }


            // 请求次数超100次，添加进禁止列表，5分钟
            if (times > 100) {
                redisTemplate.opsForValue().set("forbid_"+ip, "1", 10, TimeUnit.SECONDS);
                return false;
            }
        } else {
            // ip不存在，添加进去,有效期3分钟
            redisTemplate.opsForValue().set(ip, "1", 3L, TimeUnit.MINUTES);
            return true;
        }

        return true;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = null;
        String localIp = request.getRemoteAddr();
        String host = request.getHeader("Host");
        String realIp = request.getHeader("X-Real-IP");
        String xFordFor = request.getHeader("X-Forwarded-For");
        String remoteHJost = request.getHeader("REMOTE-HOST");
        String readPort = request.getHeader("X-Real-PORT");
//        System.out.println("localIp:" + localIp);
//        System.out.println("host:" + host);
//        System.out.println("realIp:" + realIp);
//        System.out.println("xFordFor:" + xFordFor);
//        System.out.println("remoteHJost:" + remoteHJost);
//        System.out.println("readPort:" + readPort);
        Date date = new Date();
        VisitorCount visitorCount = null;
        if (realIp != null) {
            ip = realIp;
            visitorCount = mapper.selectByIpToday(ip);
        }else if (xFordFor != null){
            ip = xFordFor;
            visitorCount = mapper.selectByIpToday(ip);
        } else {
            ip = localIp;
            visitorCount = mapper.selectByIpToday(ip);
        }

        if (visitorCount == null) {
            if (ip != null) {
                // 插入ip记录表
                mapper.insert(ip);
                // 插入pv表
                mapper.insertPv(ip);
            }
        }

        // 统计pv
        if (ip != null) {
            // 判断是否已经存在pv，存在则直接更新，不存在则插入
            PvCount pvCount = mapper.selectByIp(ip);
            if (pvCount == null) {
                mapper.insertPv(ip);
                mapper.updatePv(ip);
            } else {
                // 只更新当前的ip访问次数
                mapper.updatePv(ip);
            }

        }
        // 设置ip的3分钟内请求次数记录
        if (checkSpider(ip)) {
            return true;
        } else {
            response.sendRedirect("/noSpider.html");
            return false;
        }


    }



}
