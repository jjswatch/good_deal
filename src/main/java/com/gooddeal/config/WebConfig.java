package com.gooddeal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                		"/api/auth/**",
                        "/api/admin/login",
                        "/api/products/**",
                        "/api/price-reports/product/**",
                        "/api/categories/**",
                        "/api/prices/**",
                        "/api/stores/**"
                );
    }
}


