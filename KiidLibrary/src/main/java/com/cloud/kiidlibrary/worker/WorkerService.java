package com.cloud.kiidlibrary.worker;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.Future;

@Component
public class WorkerService
{
    @Autowired
    private NextCloudWorker nextCloudWorker;


    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerService.class);

    public void runAsyncNextCloudWorker(NextcloudConnector nc, InputStream is, String ncPath, int retry) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        LOGGER.info(" start of executing all async workers ");
        nextCloudWorker.execute(nc, is, ncPath, retry);
        LOGGER.info(" Async workers: total execution time [" + ((System.currentTimeMillis() - startTime))/1000 + "] seconds ");
    }
}