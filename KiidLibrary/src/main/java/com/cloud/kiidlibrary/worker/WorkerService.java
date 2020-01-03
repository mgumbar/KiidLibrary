package com.cloud.kiidlibrary.worker;

import com.cloud.kiidlibrary.model.Kiid;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;

@Component
public class WorkerService
{
    @Autowired
    private NextCloudWorker nextCloudWorker;
    @Autowired
    private OcrWorker ocrWorker;


    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerService.class);

    public void runAsyncNextCloudWorker(NextcloudConnector nc, InputStream is, String ncPath, int retry) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        LOGGER.info(" start of executing all async workers ");
        nextCloudWorker.execute(nc, is, ncPath, retry);
        LOGGER.info(" Async workers: total execution time [" + ((System.currentTimeMillis() - startTime))/1000 + "] seconds ");
    }

    public void runBulkOCRWorker(NextcloudConnector nc, PDDocument document, String ncPath, Kiid kiid, int retry) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        LOGGER.info(" start of executing all async workers ");
        ocrWorker.processImgPdf(nc, document, ncPath, kiid, retry);
        LOGGER.info(" Async workers: total execution time [" + ((System.currentTimeMillis() - startTime))/1000 + "] seconds ");
    }

    public void runOCRWorker(NextcloudConnector nc, List<File> files, String ncPath, Kiid kiid, int retry) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        LOGGER.info(" start of executing all async workers ");
        ocrWorker.execute(nc, files, ncPath, kiid, retry);
        LOGGER.info(" Async workers: total execution time [" + ((System.currentTimeMillis() - startTime))/1000 + "] seconds ");
    }
}