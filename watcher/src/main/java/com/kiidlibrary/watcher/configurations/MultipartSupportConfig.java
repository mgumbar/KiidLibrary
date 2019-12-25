package com.kiidlibrary.watcher.configurations;

import feign.codec.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.*;

@Configuration
public class MultipartSupportConfig {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    @Primary
    @Scope("prototype")
    public Encoder feignEncoder() {
//        return new SpringFormEncoder();
//        return new SpringFormEncoder(new SpringEncoder(messageConverters));
        return new FeignSpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public feign.Logger.Level multipartLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

}