package com.kiidlibrary.watcher.proxies;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Configuration
@Component
//@FeignClient(name = "zuul", url = "192.168.1.16:9004")
@FeignClient(name = "zuul", url = "localhost:9004")
@RibbonClient(name = "kiidlibrary")
public interface PostFileProxy {

    @RequestMapping(value = "/kiidlibrary/kiidfile", consumes = {"multipart/form-data"}, method = RequestMethod.POST)
    void upload(@RequestPart(name="file") MultipartFile file);

}

