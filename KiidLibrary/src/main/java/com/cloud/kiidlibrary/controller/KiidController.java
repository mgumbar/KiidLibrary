package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.configurations.ApplicationPropertiesConfiguration;
import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.exceptions.NotFoundException;
import com.cloud.kiidlibrary.model.Kiid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/kiid")
public class KiidController {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final KiidRepository kiidRepository;
    @Autowired
    private ApplicationPropertiesConfiguration appProperties;


    public KiidController(KiidRepository kiidRepository) {
        this.kiidRepository = kiidRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Kiid> getAllKiids() {
        LOG.info("Getting all kiids.");
        List<Kiid> kiids = kiidRepository.findAll().subList(0, appProperties.getMaxitems());
        if(kiids.isEmpty())  throw new NotFoundException("No kiid found");
        return kiids;
    }

    @RequestMapping(value = "/{kiidId}", method = RequestMethod.GET)
    public Optional<Kiid> getKiid(@PathVariable String kiidId) {
        LOG.info("Getting kiid with ID: {}.", kiidId);
        Optional<Kiid> kiidO = kiidRepository.findById(kiidId);
        if (!kiidO.isPresent()) throw new NotFoundException("Kiid with Id not found" + kiidId);
        return kiidO;
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Kiid addNewKiid(@Valid @RequestBody Kiid kiid) {
        LOG.info("Saving kiid.");
        return kiidRepository.save(kiid);
    }


    @RequestMapping(value = "/settings/{kiidId}", method = RequestMethod.GET)
    public Object getAllKiidSettings(@PathVariable String kiidId) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid != null) {
            return kiid.get().getKiidProperties();
        } else {
            return "Kiid not found.";
        }
    }

    @RequestMapping(value = "/settings/{kiidId}/{key}", method = RequestMethod.GET)
    public String getKiidProperties(@PathVariable String kiidId, @PathVariable String key) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid != null) {
            return kiid.get().getKiidProperties().get(key);
        } else {
            return "Kiid not found.";
        }
    }


    @RequestMapping(value = "/settings/{kiidId}/{key}/{value}", method = RequestMethod.GET)
    public String addKiidProperties(@PathVariable String kiidId, @PathVariable String key, @PathVariable String value) {
        Optional<Kiid> kiidO = kiidRepository.findById(kiidId);
        if (kiidO.isPresent()) {
            Kiid kiid = kiidO.get();
            kiid.getKiidProperties().put(key, value);
            kiidRepository.save(kiid);
            return "Key added";
        } else {
            return "Kiid not found.";
        }
    }
}
