package com.cloud.kiidlibrary.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("my-configs")
public class ApplicationPropertiesConfiguration {
    private  int maxitems;

    public int getMaxitems() {
        return maxitems;
    }

    public void setMaxitems(int maxitems) {
        this.maxitems = maxitems;
    }
}
