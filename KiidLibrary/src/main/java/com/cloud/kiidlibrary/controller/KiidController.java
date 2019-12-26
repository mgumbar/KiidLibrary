package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.configurations.ApplicationPropertiesConfiguration;
import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.exceptions.NotFoundException;
import com.cloud.kiidlibrary.model.Kiid;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping(value = "/kiid")
public class KiidController implements HealthIndicator {
    @Value("${nextCloud.Ip}")
    private String nextCloudIp;
    @Value("${nextCloud.port}")
    private int nextCloudPort;
    @Value("${nextCloud.user}")
    private String nextCloudUser;
    @Value("${nextCloud.pwd}")
    private String nextCloudPwd;
    @Value("${nextCloud.useHttps}")
    private boolean nextCloudUseHttps;
    @Value("${nextCloud.kiidFolder}")
    private String nextCloudKiidFolder;

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


//    @RequestMapping(value = "/create", method = RequestMethod.POST)
//    public Kiid addNewKiid(@Valid @RequestBody Kiid kiid) {
//        LOG.info("Saving kiid.");
//        return kiidRepository.save(kiid);
//    }


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


    @ApiOperation("file upload receive part")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public boolean uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        if (file.isEmpty())
        {
            return  false;
        }
        else
        {
            NextcloudConnector nxt = new NextcloudConnector(this.nextCloudIp, this.nextCloudUseHttps, this.nextCloudPort, this.nextCloudUser, this.nextCloudPwd);
            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            if(!nxt.folderExists(this.nextCloudKiidFolder))
            {
                nxt.createFolder(this.nextCloudKiidFolder);
            }
            String nextCloudPath = this.nextCloudKiidFolder + "/" + UUID.randomUUID() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            nxt.uploadFile(inputStream, nextCloudPath);


            System.out.println("File names is + " + file.getOriginalFilename());
            PDDocument doc = PDDocument.load(fileBytes);
//            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDDocumentInformation info = doc.getDocumentInformation();
//            System.out.println( "Page Count=" + doc.getNumberOfPages() );
//            System.out.println( "Creation Date=" + info.getCreationDate() );
//            System.out.println( "Modification Date=" + info.getModificationDate());
//            System.out.println( "Trapped=" + info.getTrapped() );

            String properties = info.getKeywords();
            Kiid kiid = new Kiid(nextCloudPath, info.getTitle(), info.getAuthor(), info.getSubject(), file.getOriginalFilename(), info.getProducer(), info.getCreator(), this.convertProperties(info.getKeywords()));
            kiidRepository.save(kiid);
            return  true;
        }
    }

    @Override
    public Health health() {
        List<Kiid> products = kiidRepository.findAll();

        if(products.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    } //suite du code ... }

    private Map<String, String> convertProperties(String keywords)
    {
        Map<String,String> map = new HashMap<>();
        if (keywords != null && ! keywords.isEmpty()) {
            String[] keyValuePairs = keywords.split(";");
            for(String pair : keyValuePairs)
            {
                String[] entry = pair.split("=");
                map.put(entry[0].trim(), entry[1].trim());
            }
        }
        return  map;
    }
}
