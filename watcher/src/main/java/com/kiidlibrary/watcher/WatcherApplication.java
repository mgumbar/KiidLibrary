package com.kiidlibrary.watcher;

import com.kiidlibrary.watcher.thread.fileWatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.kiidlibrary.watcher.proxies"}) // NOM DU PACKAGE PROXY
@ComponentScan({"com.kiidlibrary.watcher.proxies"})
public class WatcherApplication {

	public static void main(String[] args) {
		fileWatcher fw = new fileWatcher();
		fw.start();
		SpringApplication.run(WatcherApplication.class, args);
	}

}
