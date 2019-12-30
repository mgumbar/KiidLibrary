package com.cloud.kiidlibrary.worker;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@EnableAsync
@Component
public class NextCloudWorker
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NextCloudWorker.class);

    @Async
    public void execute(NextcloudConnector nc, InputStream is, String ncPath, int retry) throws InterruptedException {
        LOGGER.info(" AsyncWorker: current thread [" + Thread.currentThread().getName() + "]");
        try {
            if (retry > 5)
                return;
            nc.uploadFile(is, ncPath);
        } catch (NextcloudApiException ex) {

            Thread.sleep(retry * 1000);
            this.execute(nc, is, ncPath, retry++);
            LOGGER.error(" sleeping thread interrupted retry(" + retry + "):", ex);
        }
        LOGGER.info(" AsyncWorker: completed [" + Thread.currentThread().getName() + "]");
    }
}