package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.Advertisment;
import com.java2nb.novel.mapper.AdverMapper;
import com.java2nb.novel.service.AdverService;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Random;


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
    public int isShowAdver(HttpServletRequest request, String id) {
        if (id == null) {
            return 1;
        }
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
            List<String> advertisment = mapper.selectIp(ip, Integer.valueOf(id));
            if (advertisment.size() != 0) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }


    /**
     * 对当天点击过广告的ip以及广告，进行记录
     * @param request
     */
    @Override
    public void clickRecode(HttpServletRequest request, String id) {
        if (id == null) {
            return;
        }
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
            List<String> i = mapper.selectIp(ip, Integer.valueOf(id));
            if (i.size() == 0) {
                // 如果不存在,则插入
                mapper.insert(ip, Integer.valueOf(id));
            }
        }
    }

    @Override
    public Advertisment selectAdver(HttpServletRequest request) {
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
            // 获取到ip,把已经点过的排除掉
            List<Advertisment> advertisment = mapper.selectAdverByIpId(ip);
            if (advertisment.size() == 0) {
                return new Advertisment();
            }
            return advertisment.get(RandomUtils.nextInt(advertisment.size()));

        } else {
            // 获取不到ip，随机选一个发送
            List<Advertisment> advertisment = mapper.selectAdver();
            if (advertisment.size() == 0) {
                return new Advertisment();
            }
            return advertisment.get(RandomUtils.nextInt(advertisment.size()));
        }



    }


}
