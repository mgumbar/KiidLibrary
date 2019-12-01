package com.kiidlibrary.ThymeleafUI.proxies;

import com.kiidlibrary.ThymeleafUI.beans.KiidBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

//NOM DU MICRO SERVICE A APPELLER A RENSEIGNER DANS APPLICATION PROPERTIES DE kiidlibrary
@FeignClient(name = "kiidlibrary", url = "localhost:9090")
public interface MicroservicesKiid {

    @RequestMapping(value = "/kiid", method = RequestMethod.GET)
    public List<KiidBean> getAllKiids();

    @RequestMapping(value = "/{kiidId}", method = RequestMethod.GET)
    public KiidBean getKiid(@PathVariable("id") String kiidId);
}
