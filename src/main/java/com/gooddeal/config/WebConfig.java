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
                        
                        // ⭐ 商品公開瀏覽
                        "/api/products/**",
                        "/api/categories/**",
                        "/api/stores/**",
                        "/api/common/**",

                        // ⭐ 商品頁會用到
                        "/api/prices/**",
                        "/api/price-reports/product/**",
                        "/api/history/**" 
                );
    }
}


