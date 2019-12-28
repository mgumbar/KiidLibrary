package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.configurations.ApplicationPropertiesConfiguration;
import com.cloud.kiidlibrary.dal.KiidDALImpl;
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
    private final KiidDALImpl kiidDAL;

    @Autowired
    private ApplicationPropertiesConfiguration appProperties;


    public KiidController(KiidRepository kiidRepository, KiidDALImpl kiidDAL) {
        this.kiidRepository = kiidRepository;
        this.kiidDAL = kiidDAL;
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

    @RequestMapping(value = "/cloudId/{cloudId}", method = RequestMethod.GET)
    public Kiid getKiidByCloudId(@PathVariable String cloudId) {
//        LOG.info("Getting kiid with ID: {}.", cloudId);
        Kiid kiid = kiidDAL.getByCloudId(this.nextCloudKiidFolder + "/" + cloudId + ".pdf" );
        if (kiid == null) throw new NotFoundException("Kiid with Id not found" + cloudId);
        return kiid;
    }

    @RequestMapping(value = "/cloudId/{cloudId}", method = RequestMethod.DELETE)
    public boolean deleteKiidByCloudId(@PathVariable String cloudId) {
        String filePath = this.nextCloudKiidFolder + "/" + cloudId + ".pdf";
//        LOG.info("Getting kiid with ID: {}.", filePath);
        Kiid kiid = kiidDAL.getByCloudId(filePath);
        if (kiid == null) throw new NotFoundException("Kiid with Id not found" + cloudId);
        this.nxt().removeFile(filePath);
        kiid.setDeleted(true);
        kiidRepository.save(kiid);
        return kiid.getDeleted();
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
    public Kiid uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        if (file.isEmpty())
        {
            return  null;
        }
        else
        {

            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            if(!this.nxt().folderExists(this.nextCloudKiidFolder))
            {
                this.nxt().createFolder(this.nextCloudKiidFolder);
            }
            String uuid = UUID.randomUUID().toString();
            String nextCloudPath = this.nextCloudKiidFolder + "/" + uuid + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            this.nxt().uploadFile(inputStream, nextCloudPath);


            System.out.println("File names is + " + file.getOriginalFilename());
            PDDocument doc = PDDocument.load(fileBytes);
//            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDDocumentInformation info = doc.getDocumentInformation();
//            System.out.println( "Page Count=" + doc.getNumberOfPages() );
//            System.out.println( "Creation Date=" + info.getCreationDate() );
//            System.out.println( "Modification Date=" + info.getModificationDate());
//            System.out.println( "Trapped=" + info.getTrapped() );

            Kiid kiid = new Kiid(nextCloudPath, info.getTitle(), info.getAuthor(), info.getSubject(), file.getOriginalFilename(), info.getProducer(), info.getCreator(), Kiid.convertProperties(info.getKeywords()));
            kiidRepository.save(kiid);
            return  kiid;
        }
    }

    @Override
    public Health health() {
        List<Kiid> products = kiidRepository.findAll();

        if(products.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }

    private  NextcloudConnector nxt()
    {
        return new NextcloudConnector(this.nextCloudIp, this.nextCloudUseHttps, this.nextCloudPort, this.nextCloudUser, this.nextCloudPwd);
    }
}
