package com.cloud.kiidlibrary.bean;

import org.aarboard.nextcloud.api.NextcloudConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton") // change to prototype
@PropertySource("classpath:bootstrap.properties")
public class NextCloud {

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

    private NextcloudConnector nextcloudConnector = null;


    public NextCloud() {
        if (this.nextCloudIp != null) {
            nextcloudConnector = new NextcloudConnector(this.nextCloudIp, this.nextCloudUseHttps, this.nextCloudPort, this.nextCloudUser, this.nextCloudPwd);
        }
    }

    public NextcloudConnector getNextcloudConnector() {
        if (this.nextcloudConnector == null){
            this.nextcloudConnector = new NextcloudConnector(this.nextCloudIp, this.nextCloudUseHttps, this.nextCloudPort, this.nextCloudUser, this.nextCloudPwd);
        }
        return this.nextcloudConnector;
    }
}