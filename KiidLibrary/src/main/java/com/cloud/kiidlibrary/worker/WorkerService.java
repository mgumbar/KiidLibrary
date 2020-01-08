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
import java.text.MessageFormat;
import java.util.List;

@Component
public class WorkerService {
    public static final String START_OF_EXECUTING_ALL_ASYNC_WORKERS = " start of executing all async workers ";
    public static final String ASYNC_WORKERS_TOTAL_EXECUTION_TIME_0_SECONDS = " Async workers: total execution time [{0}] seconds ";
    @Autowired
    private NextCloudWorker nextCloudWorker;
    @Autowired
    private OcrWorker ocrWorker;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerService.class);

    public void runAsyncNextCloudWorker(NextcloudConnector nc, InputStream is, String ncPath, int retry) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(START_OF_EXECUTING_ALL_ASYNC_WORKERS);
        nextCloudWorker.execute(nc, is, ncPath, retry);
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(MessageFormat.format(ASYNC_WORKERS_TOTAL_EXECUTION_TIME_0_SECONDS, (System.currentTimeMillis() - startTime) / 1000));
    }

    public void runBulkOCRWorker(NextcloudConnector nc, PDDocument document, String ncPath, Kiid kiid, int retry) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(START_OF_EXECUTING_ALL_ASYNC_WORKERS);
        ocrWorker.processImgPdf(nc, document, ncPath, kiid, retry);
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(MessageFormat.format(ASYNC_WORKERS_TOTAL_EXECUTION_TIME_0_SECONDS, (System.currentTimeMillis() - startTime) / 1000));
    }

    public void runOCRWorker(NextcloudConnector nc, List<File> files, String ncPath, Kiid kiid, int retry) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(START_OF_EXECUTING_ALL_ASYNC_WORKERS);
        ocrWorker.execute(nc, files, ncPath, kiid, retry);
        if (!LOGGER.isDebugEnabled())
            LOGGER.info(MessageFormat.format(ASYNC_WORKERS_TOTAL_EXECUTION_TIME_0_SECONDS, (System.currentTimeMillis() - startTime) / 1000));
    }
}