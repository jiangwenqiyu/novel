package com.java2nb.novel.core.config;

import com.java2nb.novel.core.interceptor.VisitorsInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class VisitorInterceptorConfig implements WebMvcConfigurer {

    @Bean
    public HandlerInterceptor getMyInterceptor() {
        return new VisitorsInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HandlerInterceptor interceptor = getMyInterceptor();
        // 配置白名单
        List<String> patterns = new ArrayList<>();
        patterns.add("/css/**");
        patterns.add("/images/**");
        patterns.add("/javascript/**");
        patterns.add("/layui/**");
        patterns.add("/mobile/**");
        patterns.add("/wangEditor/**");

        // 黑名单
        List<String> black = new ArrayList<>();
        black.add("/");
        black.add("/book/**.html");
        black.add("/book/**/**.html");



        registry.addInterceptor(interceptor).addPathPatterns(black);
    }
}
