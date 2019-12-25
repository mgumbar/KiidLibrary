package com.kiidlibrary.watcher.thread;

import com.kiidlibrary.watcher.proxies.PostFileProxy;
import feign.form.ContentType;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.apache.http.*;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.mock.web.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.List;

public class fileWatcher extends Thread{
    @Autowired
    private PostFileProxy uploadService;
    public void run() {
        long startTime = System.currentTimeMillis();
        int i = 0;
        while (true) {
            System.out.println(this.getName() + ": New Thread is running..." + i++);

            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                String inPath = "C:\\Users\\hp_envy\\Downloads";
                Path path = Paths.get(inPath);

                path.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE);

                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        System.out.println(
                                "Event kind:" + event.kind()
                                        + ". File affected: " + event.context() + ".");
//                        String fileName = event.context().toString();
//                        String contentType = "text/plain";
//                        byte[] content = null;
//                        try {
//                            content = Files.readAllBytes(Paths.get(inPath + "\\" + event.context()));
//                        } catch (final IOException e) {
//                        }
//                        MultipartFile multi = new MockMultipartFile(fileName,
//                                fileName, contentType, content);
//                        this.uploadService.upload(multi);
                        File file = new File(inPath + "\\" + event.context());
                        if (file.exists())
                        {
                            System.out.println("file exists");
                            this.uploadFile(inPath + "\\" + event.context(), "http://localhost:9090/kiid/upload");
                        }
                        else
                        {
                            System.out.println("file does not exists");
                        }
                    }
                    key.reset();
                }

                //Wait for one sec so it doesn't print too fast
                Thread.sleep(1000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadFile(String filePath, String url) throws IOException {
        try {
            String charset = "UTF-8";
            File uploadFile1 = new File(filePath);
            String requestURL = url;
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

//            multipart.addHeaderField("User-Agent", "CodeJava");
//            multipart.addHeaderField("Test-Header", "Header-Value");
//
//            multipart.addFormField("description", "Cool Pictures");
//            multipart.addFormField("keywords", "Java,upload,Spring");

            multipart.addFilePart("file", uploadFile1);

            List<String> response = multipart.finish();

            System.out.println("SERVER REPLIED:");

            for (String line : response) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
