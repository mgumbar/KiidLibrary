package com.cloud.kiidlibrary.worker;

//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;

import java.awt.Graphics2D;

import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.model.Kiid;
import net.sourceforge.tess4j.*;

import java.awt.Image;
import java.awt.image.*;
import java.io.*;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableAsync
@Component
public class OcrWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcrWorker.class);
    @Autowired
    private KiidRepository kiidRepository;

    @Async
    public void processImgPdf(NextcloudConnector nc, PDDocument document, String ncPath, Kiid kiid, int retry) throws InterruptedException, IOException {
        try {
            List<File> files = new ArrayList<File>();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int nbPage = document.getDocumentCatalog().getPages().getCount();
            System.out.println("Total files to be converted -> " + nbPage);

            String fileExtension = "png";
            int dpi = 600;

            for (int pageNumber = 0; pageNumber < nbPage; pageNumber++) {
                // Create stream object to save the output image
//            java.io.OutputStream imageStream = new java.io.FileOutputStream("Converted_Image" + pageNumber + fileExtension);

                File outPutFile = new File( kiid.getUuid() + "_" + (pageNumber) + "." + fileExtension);
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageNumber, dpi, ImageType.RGB);
                ImageIO.write(bImage, fileExtension, outPutFile);
                System.out.println(outPutFile.getAbsolutePath());
                files.add(outPutFile);
                bImage.flush();
//            outPutFile.delete();
//            imageStream.flush();
//            imageStream.close();
            }
            ;
            document.close();
            this.execute(nc, files, ncPath, kiid, 0);
        } catch (Exception ex) {
            Thread.sleep((long)retry * 1000);
            LOGGER.error(" sleeping thread interrupted retry(" + retry + "):", ex);
            this.processImgPdf(nc, document, ncPath, kiid, retry += 1);
        }
    }

    @Async
    public void execute(NextcloudConnector nc, List<File> files, String ncPath, Kiid kiid, int retry) throws InterruptedException {
        LOGGER.info(" AsyncWorker: current thread [" + Thread.currentThread().getName() + "]");
        try {
            Map<String, String> properties = kiid.getKiidProperties();
            for (int f = 0; f < files.size(); f++) {
                File file = files.get(f);
                if (retry > 5)
                    return;
                String ncPathTmp = ncPath.substring(0, ncPath.indexOf("/") + 1) + file.getName().substring(0, file.getName().lastIndexOf(".") + 1) + "txt";
                File filetmp = File.createTempFile(ncPath.substring(0, ncPath.lastIndexOf(".")), ".txt");
                System.out.println("FILE EXISTS" + file.getName());
                if (file.exists()) {
                    System.out.println("FILE EXISTS");
                }
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("C:\\Users\\hp_envy\\source\\KiidLibraryGlobal\\KiidLibrary\\src\\main\\resources\\tessdata");
//            tesseract.setLanguage("fra");
                String text = tesseract.doOCR(file);
                String lines[] = text.split("\\r?\\n");
                int i = 0;
                for (String line : lines)
                    properties.put((f + 1) + ";" + i++, line);
                InputStream newIs = new ByteArrayInputStream(StandardCharsets.UTF_16.encode(text).array());
                nc.uploadFile(newIs, ncPathTmp);
                file.delete();
            }
            kiid.setKiidProperties(properties);
            kiidRepository.save(kiid);
        } catch (NextcloudApiException | TesseractException ex) {
            Thread.sleep((long) retry * 1000);
            LOGGER.error(" sleeping thread interrupted retry(" + retry + "):", ex);
            this.execute(nc, files, ncPath, kiid, retry += 1);
        } catch (Exception ex) {
            Thread.sleep((long) retry * 1000);
            LOGGER.error(" sleeping thread interrupted retry(" + retry + "):", ex);
            this.execute(nc, files, ncPath, kiid, retry += 1);
        }
        LOGGER.info(" AsyncWorker: completed [" + Thread.currentThread().getName() + "]");
    }
}