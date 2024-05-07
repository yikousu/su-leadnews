package com.su.wemedia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 使拦截器生效
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private WmTokenInterceptor wmTokenInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(wmTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login/in");
    }
}
