package com.cloud.kiidlibrary.worker;

import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.model.Kiid;
import net.sourceforge.tess4j.*;

import java.awt.image.*;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EnableAsync
@Component
public class OcrWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcrWorker.class);
//    public static final String TESSDATA_PATH = "../KiidLibraryGlobal/KiidLibrary/src/main/resources/tessdata";
//    private static final String TESSDATA_PATH = "src/test/java/com/cloud/kiidlibrary/resources/tessdata";
    @Value("${tesseract.dataPath}")
    private String tessdataPath;

    @Autowired
    private KiidRepository kiidRepository;

    @Async
    public void processImgPdf(NextcloudConnector nc, PDDocument document, String ncPath, Kiid kiid, int retry) throws InterruptedException, IOException {
        try {
            List<File> files = new ArrayList<>();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int nbPage = document.getDocumentCatalog().getPages().getCount();
            if (!LOGGER.isDebugEnabled()) {
                LOGGER.info(MessageFormat.format("Total files to be converted -> {0}", nbPage));
            }

            String fileExtension = "png";
            int dpi = 300;

            for (int pageNumber = 0; pageNumber < nbPage; pageNumber++) {
                File outPutFile = new File(kiid.getUuid() + "_" + (pageNumber) + "." + fileExtension);
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageNumber, dpi, ImageType.RGB);
                ImageIO.write(bImage, fileExtension, outPutFile);
                files.add(outPutFile);
                bImage.flush();
            }
            document.close();
            this.execute(nc, files, ncPath, kiid, 0);
        } catch (Exception ex) {
            Thread.sleep((long) retry * 1000);
            retry += 1;
            LOGGER.error(" sleeping thread interrupted retry(" + retry + "):", ex);
            this.processImgPdf(nc, document, ncPath, kiid, retry);
        }
    }

    @Async
    public void execute(NextcloudConnector nc, List<File> files, String ncPath, Kiid kiid, int retry) throws InterruptedException {
        if (!LOGGER.isDebugEnabled()) {
            LOGGER.info(MessageFormat.format(" AsyncWorker: current thread [{0}]", Thread.currentThread().getName()));
        }
        try {
            Map<String, String> properties = kiid.getKiidProperties();
            for (int f = 0; f < files.size(); f++) {
                File file = files.get(f);
                if (retry > 5)
                    return;
                String ncPathTmp = ncPath.substring(0, ncPath.indexOf('/') + 1) + file.getName().substring(0, file.getName().lastIndexOf('.') + 1) + "txt";
                if (!LOGGER.isDebugEnabled()) {
                    LOGGER.info(MessageFormat.format("FILE EXISTS: [{0}]", file.getName()));
                }

                Tesseract tesseract = new Tesseract();
                String filePath = getClass().getResource("/bootstrap.properties").getFile();
                if (!LOGGER.isDebugEnabled()) {
                    LOGGER.info(MessageFormat.format("TESSDATA FILE PATH: [{0}]", filePath));
                }
                filePath = filePath.replace("bootstrap.properties", "tessdata").substring(1);
                tesseract.setDatapath(filePath);
                tesseract.setLanguage("eng");
                String text = tesseract.doOCR(file);
                String[] lines = text.split("\\r?\\n");
                int i = 0;
                for (String line : lines)
                    properties.put((f + 1) + ";" + i++, line);
                InputStream newIs = new ByteArrayInputStream(StandardCharsets.UTF_16.encode(text).array());
                nc.uploadFile(newIs, ncPathTmp);
                Files.delete(file.toPath());
                if (file.exists() && !LOGGER.isDebugEnabled())
                    LOGGER.error(MessageFormat.format("Error while deleting file: {0}", file.getAbsolutePath()));
            }
            kiid.setKiidProperties(properties);
            kiidRepository.save(kiid);
        } catch (NextcloudApiException | TesseractException ex) {
            Thread.sleep((long) retry * 1000);
            retry += 1;
            LOGGER.error(MessageFormat.format(" NC ||Tessereact; sleeping thread interrupted retry({0}):", retry), ex);
            this.execute(nc, files, ncPath, kiid, retry);
        } catch (Exception ex) {
            Thread.sleep((long) retry * 1000);
            retry += 1;
            LOGGER.error(MessageFormat.format(" sleeping thread interrupted retry({0}):", retry), ex);
            this.execute(nc, files, ncPath, kiid, retry);
        }
        if (!LOGGER.isDebugEnabled()) {
            LOGGER.info(MessageFormat.format("AsyncWorker: completed [{0}]", Thread.currentThread().getName()));
        }
    }
}