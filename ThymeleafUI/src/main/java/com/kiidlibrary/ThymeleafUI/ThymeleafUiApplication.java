package com.kiidlibrary.ThymeleafUI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients("com.kiidlibrary.ThymeleafUI.proxies") // NOM DU PACKAGE PROXY
@EnableDiscoveryClient
public class ThymeleafUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThymeleafUiApplication.class, args);
	}

}
