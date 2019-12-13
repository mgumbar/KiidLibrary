package com.kiidlibrary.ThymeleafUI.controller;

import com.kiidlibrary.ThymeleafUI.beans.KiidBean;
import com.kiidlibrary.ThymeleafUI.proxies.MicroservicesKiid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
//@RequestMapping(value = "/UI")
public class UIController {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MicroservicesKiid microservicesKiid;
    @RequestMapping("/")
    public String accueil(Model model){
        log.info("TRYING to get all kiids");
        List<KiidBean> kiids = microservicesKiid.getAllKiids();
        model.addAttribute("kiids", kiids);
        return "Accueil";
    }
}
