package com.kiidlibrary.ThymeleafUI.proxies;

import com.kiidlibrary.ThymeleafUI.beans.KiidBean;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

//NOM DU MICRO SERVICE A APPELLER A RENSEIGNER DANS APPLICATION PROPERTIES DE kiidlibrary
//@FeignClient(name = "kiidlibrary", url = "192.168.1.16:9090")
@FeignClient(name = "zuul", url = "192.168.1.16:9004")
@RibbonClient(name = "kiidlibrary")
public interface MicroservicesKiid {

    @RequestMapping(value = "/kiidlibrary/kiid", method = RequestMethod.GET)
    public List<KiidBean> getAllKiids();

    //@GetMapping( value = "/{kiidId}")
    @RequestMapping(value = "/{kiidId}", method = RequestMethod.GET)
    public KiidBean getKiid(@PathVariable("id") String kiidId);
}
