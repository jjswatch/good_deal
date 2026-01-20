package com.gooddeal.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gooddeal.security.JwtAuthFilter;

@Configuration
public class FilterConfig {

    @Bean
    FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean =
                new FilterRegistrationBean<>();

        bean.setFilter(new JwtAuthFilter());
        bean.addUrlPatterns("/api/*"); // 所有 API
        bean.setOrder(1);

        return bean;
    }
}