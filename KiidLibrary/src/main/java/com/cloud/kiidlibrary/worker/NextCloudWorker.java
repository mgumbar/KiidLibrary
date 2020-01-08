package com.cloud.kiidlibrary.worker;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.MessageFormat;

@EnableAsync
@Component
public class NextCloudWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(NextCloudWorker.class);

    @Async
    public void execute(NextcloudConnector nc, InputStream is, String ncPath, int retry) throws InterruptedException {
        if (!LOGGER.isDebugEnabled()) {
            LOGGER.info(MessageFormat.format("AsyncWorker: current thread [{0}]", Thread.currentThread().getName()));
        }
        try {
            if (retry > 5)
                return;
            String folderPath = ncPath.substring(0, ncPath.indexOf('/'));
            if (!nc.folderExists(folderPath)) {
                nc.createFolder(folderPath);
            }
            nc.uploadFile(is, ncPath);
        } catch (NextcloudApiException ex) {

            Thread.sleep((long) retry * 1000);
            retry += 1;
            this.execute(nc, is, ncPath, retry);
            LOGGER.error(MessageFormat.format(" sleeping thread interrupted retry({0})", retry), ex);
        }
        if (!LOGGER.isDebugEnabled()) {
            LOGGER.info(MessageFormat.format("AsyncWorker: completed [{0}]", Thread.currentThread().getName()));
        }
    }
}