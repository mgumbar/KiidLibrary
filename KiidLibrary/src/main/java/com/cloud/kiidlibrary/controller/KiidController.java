package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.bean.NextCloud;
import com.cloud.kiidlibrary.configurations.ApplicationPropertiesConfiguration;
import com.cloud.kiidlibrary.dal.KiidDALImpl;
import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.exceptions.NotFoundException;
import com.cloud.kiidlibrary.model.Kiid;
import com.cloud.kiidlibrary.worker.WorkerService;
import io.swagger.annotations.ApiOperation;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.apache.pdfbox.pdmodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/kiid")
public class KiidController implements HealthIndicator {
    public static final String KIID_WITH_ID_NOT_FOUND = "Kiid with Id not found";
    public static final String PATH_DELIMITER = "/";
    public static final String PDF_FILE_EXTENSION = ".pdf";
    public static final String KIID_NOT_FOUND = "Kiid not found.";
    private static final Logger log = LoggerFactory.getLogger(KiidController.class);

    private final KiidRepository kiidRepository;
    private final KiidDALImpl kiidDAL;

    @Value("${nextCloud.kiidFolder}")
    private String nextCloudKiidFolder;

    @Autowired
    private ApplicationPropertiesConfiguration appProperties;
    @Autowired
    private NextCloud nextCloud;

    @Autowired
    private WorkerService workerService;

    public KiidController(KiidRepository kiidRepository, KiidDALImpl kiidDAL) {
        this.kiidRepository = kiidRepository;
        this.kiidDAL = kiidDAL;
    }

    @GetMapping()
    public List<Kiid> getAllKiids() {
        log.info("Getting all kiids.");
        List<Kiid> kiids = kiidRepository.findAll().subList(0, appProperties.getMaxitems());
        if (kiids.isEmpty()) throw new NotFoundException("No kiid found");
        return kiids;
    }

    @GetMapping(path = "/{kiidId}")
    public Optional<Kiid> getKiid(@PathVariable String kiidId) {
        log.info("Getting kiid with ID: {}.", kiidId);
        Optional<Kiid> kiidO = kiidRepository.findById(kiidId);
        if (!kiidO.isPresent()) throw new NotFoundException(KIID_WITH_ID_NOT_FOUND + kiidId);
        return kiidO;
    }

    @GetMapping(path = "/cloudId/{cloudId}")
    public Kiid getKiidByCloudId(@PathVariable String cloudId) {
        log.info("Getting kiid with ID: {}.", cloudId);
        Kiid kiid = kiidDAL.getByCloudId(this.nextCloudKiidFolder + PATH_DELIMITER + cloudId + PDF_FILE_EXTENSION);
        if (kiid == null) throw new NotFoundException(KIID_WITH_ID_NOT_FOUND + cloudId);
        return kiid;
    }

    @DeleteMapping(path = "/cloudId/{cloudId}")
    public boolean deleteKiidByCloudId(@PathVariable String cloudId, String fileExtension) {
        if (fileExtension.isEmpty())
            fileExtension = PDF_FILE_EXTENSION;
        String filePath = this.nextCloudKiidFolder + PATH_DELIMITER + cloudId + fileExtension;
        log.info("Deleting kiid with ID: {}.", filePath);
        Kiid kiid = kiidDAL.getByCloudId(filePath);
        if (kiid == null) throw new NotFoundException(KIID_WITH_ID_NOT_FOUND + cloudId);
        this.nextCloud.getNextcloudConnector().removeFile(filePath);
        kiid.setDeleted(true);
        kiidRepository.save(kiid);
        return kiid.getDeleted();
    }

//    @RequestMapping(value = "/create", method = RequestMethod.POST)
//    public Kiid addNewKiid(@Valid @RequestBody Kiid kiid) {
//        LOG.info("Saving kiid.");
//        return kiidRepository.save(kiid);
//    }


    @GetMapping(path = "/properties/{kiidId}")
    public Object getAllKiidSettings(@PathVariable String kiidId) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid.isPresent()) {
            return kiid.get().getKiidProperties();
        } else {
            return KIID_NOT_FOUND;
        }
    }

    @GetMapping(path = "/property/{kiidId}/{key}")
    public String getKiidProperty(@PathVariable String kiidId, @PathVariable String key) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid.isPresent()) {
            return kiid.get().getKiidProperties().get(key);
        } else {
            return KIID_NOT_FOUND;
        }
    }


    @GetMapping(path = "/property/{kiidId}/{key}/{value}")
    public String addKiidProperty(@PathVariable String kiidId, @PathVariable String key, @PathVariable String value) {
        Optional<Kiid> kiidO = kiidRepository.findById(kiidId);
        if (kiidO.isPresent()) {
            Kiid kiid = kiidO.get();
            kiid.getKiidProperties().put(key, value);
            kiidRepository.save(kiid);
            return "Key added";
        } else {
            return KIID_NOT_FOUND;
        }
    }

    @ApiOperation("file upload receive part")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Kiid uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException, InterruptedException {
        byte[] fileBytes = file.getBytes();
        try {
            // SAVE IN THE DB
            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            String uuid = UUID.randomUUID().toString();
            String nextCloudPath = this.nextCloudKiidFolder + PATH_DELIMITER + uuid + '.' + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            if (log.isDebugEnabled()) {
                log.info(MessageFormat.format("File names is : {0} ", file.getOriginalFilename()));
            }
            Kiid kiid = null;
            if (file.getOriginalFilename().toUpperCase().endsWith("PDF")) {
                PDDocument doc = PDDocument.load(fileBytes);
                PDDocumentInformation info = doc.getDocumentInformation();
                String keyWords = info.getKeywords();
                if (keyWords != null && !keyWords.isEmpty())
                    kiid = new Kiid(nextCloudPath, info, file.getOriginalFilename(), Kiid.convertProperties(keyWords));

                else {
                    kiid = new Kiid(nextCloudPath, file.getOriginalFilename());
                    if (kiid.getKiidProperties().size() == 0) {
                        kiid = kiidRepository.save(kiid);
                        this.workerService.runBulkOCRWorker(this.nextCloud.getNextcloudConnector(), doc, nextCloudPath, kiid, 0);
                    }
                }
            } else {
                kiid = new Kiid(nextCloudPath, file.getOriginalFilename());
            }
            kiid = kiidRepository.save(kiid);

            // OCR
            this.workerService.runAsyncNextCloudWorker(this.nextCloud.getNextcloudConnector(), inputStream, nextCloudPath, 0);
            if (!file.getOriginalFilename().toUpperCase().endsWith("PDF")) {
                this.sendImageForOcrAndSaveInNextCloud(file, nextCloudPath, kiid);
            }
            return kiid;
        } catch (NextcloudApiException | InterruptedException e) {
            log.error(e.getMessage(), e);
            log.error(Arrays.toString(e.getStackTrace()));
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void sendImageForOcrAndSaveInNextCloud(MultipartFile file, String nextCloudPath, Kiid kiid) throws IOException, InterruptedException {
        File fileConverted = convert(file);
        List<File> tmpFiles = new ArrayList<>();
        tmpFiles.add(fileConverted);
        this.workerService.runOCRWorker(this.nextCloud.getNextcloudConnector(), tmpFiles, nextCloudPath, kiid, 0);
    }

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try {
            if (convFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(convFile);
                try {
                    fos.write(file.getBytes());
                    return convFile;
                } finally {
                    fos.close();
                }

            } else {
                log.warn("File already exists {0}.", file.getOriginalFilename());
            }

        } catch (Exception e) {
            log.error(MessageFormat.format("Error while creating file {0}.", file.getOriginalFilename()), e);
            throw e;
        }
        return convFile;
    }

    @Override
    public Health health() {
        List<Kiid> products = kiidRepository.findAll();

        if (products.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}
