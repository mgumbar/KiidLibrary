package com.kiidlibrary.watcher.thread;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class fileProcessor extends Thread{

    public fileProcessor(String filePath, String url) throws IOException {
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
        }finally {
            System.out.println("THREAD END: " + filePath);
        }
    }
}
