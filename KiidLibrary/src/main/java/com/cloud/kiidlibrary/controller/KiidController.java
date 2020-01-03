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
import java.util.*;

@RestController
@RequestMapping(value = "/kiid")
public class KiidController implements HealthIndicator {
    private final Logger log = LoggerFactory.getLogger(getClass());

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

    @RequestMapping(method = RequestMethod.GET)
    public List<Kiid> getAllKiids() {
        log.info("Getting all kiids.");
        List<Kiid> kiids = kiidRepository.findAll().subList(0, appProperties.getMaxitems());
        if (kiids.isEmpty()) throw new NotFoundException("No kiid found");
        return kiids;
    }

    @RequestMapping(value = "/{kiidId}", method = RequestMethod.GET)
    public Optional<Kiid> getKiid(@PathVariable String kiidId) {
        log.info("Getting kiid with ID: {}.", kiidId);
        Optional<Kiid> kiidO = kiidRepository.findById(kiidId);
        if (!kiidO.isPresent()) throw new NotFoundException("Kiid with Id not found" + kiidId);
        return kiidO;
    }

    @RequestMapping(value = "/cloudId/{cloudId}", method = RequestMethod.GET)
    public Kiid getKiidByCloudId(@PathVariable String cloudId) {
//        LOG.info("Getting kiid with ID: {}.", cloudId);
        Kiid kiid = kiidDAL.getByCloudId(this.nextCloudKiidFolder + "/" + cloudId + ".pdf");
        if (kiid == null) throw new NotFoundException("Kiid with Id not found" + cloudId);
        return kiid;
    }

    @RequestMapping(value = "/cloudId/{cloudId}", method = RequestMethod.DELETE)
    public boolean deleteKiidByCloudId(@PathVariable String cloudId) {
        String filePath = this.nextCloudKiidFolder + "/" + cloudId + ".pdf";
//        LOG.info("Getting kiid with ID: {}.", filePath);
        Kiid kiid = kiidDAL.getByCloudId(filePath);
        if (kiid == null) throw new NotFoundException("Kiid with Id not found" + cloudId);
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


    @RequestMapping(value = "/settings/{kiidId}", method = RequestMethod.GET)
    public Object getAllKiidSettings(@PathVariable String kiidId) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid.isPresent()) {
            return kiid.get().getKiidProperties();
        } else {
            return "Kiid not found.";
        }
    }

    @RequestMapping(value = "/settings/{kiidId}/{key}", method = RequestMethod.GET)
    public String getKiidProperties(@PathVariable String kiidId, @PathVariable String key) {
        Optional<Kiid> kiid = kiidRepository.findById(kiidId);
        if (kiid.isPresent()) {
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
    public Kiid uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException, InterruptedException {
        byte[] fileBytes = file.getBytes();
        List<File> pdfFilesWithoutProperties = new ArrayList<File>();
        try {
            // SAVE IN THE DB
            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            String uuid = UUID.randomUUID().toString();
            String nextCloudPath = this.nextCloudKiidFolder + "/" + uuid + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            System.out.println("File names is + " + file.getOriginalFilename());
            Kiid kiid = null;
            if (file.getOriginalFilename().toUpperCase().endsWith("PDF")) {
                PDDocument doc = PDDocument.load(fileBytes);
                //            PDDocumentCatalog catalog = doc.getDocumentCatalog();
                PDDocumentInformation info = doc.getDocumentInformation();
//                doc.close();
                //            System.out.println( "Page Count=" + doc.getNumberOfPages() );
                //            System.out.println( "Creation Date=" + info.getCreationDate() );
                //            System.out.println( "Modification Date=" + info.getModificationDate());
                //            System.out.println( "Trapped=" + info.getTrapped() );

                String keyWords = info.getKeywords();
                if (keyWords != null && !keyWords.isEmpty())
                    kiid = new Kiid(nextCloudPath, info.getTitle(), info.getAuthor(), info.getSubject(), file.getOriginalFilename(), info.getProducer(), info.getCreator(), Kiid.convertProperties(keyWords));

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
//            if (pdfFilesWithoutProperties.size() > 0) {
//                this.workerService.runOCRWorker(this.nextCloud.getNextcloudConnector(), pdfFilesWithoutProperties, nextCloudPath, kiid, 0);
//            }

            return kiid;
        } catch (NextcloudApiException | InterruptedException e) {
            log.error(e.getMessage(), e);
            log.error(e.getStackTrace().toString());
            throw e;
        }
    }

    public void sendImageForOcrAndSaveInNextCloud(MultipartFile file, String nextCloudPath, Kiid kiid) throws IOException, InterruptedException {
        File fileConverted = this.convert(file);
//        String workDir = System.getProperty("user.dir");
//        byte[] bytes = file.getBytes();
//        Path path = Paths.get(workDir + "\\" + file.getOriginalFilename());
        List<File> tmpFiles = new ArrayList<File>();
        tmpFiles.add(fileConverted);
        this.workerService.runOCRWorker(this.nextCloud.getNextcloudConnector(), tmpFiles, nextCloudPath, kiid, 0);
    }

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        try {
            fos.write(file.getBytes());
            return convFile;
        } catch (Exception e) {
            throw e;
        } finally {
            fos.close();
        }
    }

//    public List<File> convertToImg(PDDocument document, String fileName) throws IOException {
//        List<File> files = new ArrayList<File>();
//        PDFRenderer pdfRenderer = new PDFRenderer(document);
//        int nbPage = document.getDocumentCatalog().getPages().getCount();
//        System.out.println("Total files to be converted -> " + nbPage);
//
//        String fileExtension = "png";
//        int dpi = 600;
//
//        for (int pageNumber = 0; pageNumber < nbPage; pageNumber++) {
//            // Create stream object to save the output image
////            java.io.OutputStream imageStream = new java.io.FileOutputStream("Converted_Image" + pageNumber + fileExtension);
//
//            File outPutFile = new File(fileName + "_" + (pageNumber) + "." + fileExtension);
//            BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageNumber, dpi, ImageType.RGB);
//            ImageIO.write(bImage, fileExtension, outPutFile);
//            System.out.println(outPutFile.getAbsolutePath());
//            files.add(outPutFile);
//            bImage.flush();
////            outPutFile.delete();
////            imageStream.flush();
////            imageStream.close();
//        }
//        ;
//        document.close();
//        return files;
//    }

    @Override
    public Health health() {
        List<Kiid> products = kiidRepository.findAll();

        if (products.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}
