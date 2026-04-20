package com.zhuanzhuan.config;

import com.zhuanzhuan.interceptor.JwtTokenAdminInterceptor;
import com.zhuanzhuan.interceptor.JwtTokenUserInterceptor;
import com.zhuanzhuan.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering custom interceptors");

        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/login/buyer")
                .excludePathPatterns("/user/login/seller")
                .excludePathPatterns("/user/category/**")
                .excludePathPatterns("/user/goods/**")
                .excludePathPatterns("/user/review/goods/**")
                .excludePathPatterns("/user/review/seller/**");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Extending message converters");

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setObjectMapper(new JacksonObjectMapper());
            }
        }
    }
}
