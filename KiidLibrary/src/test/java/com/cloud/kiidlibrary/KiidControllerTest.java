package com.cloud.kiidlibrary;

import com.cloud.kiidlibrary.controller.KiidController;
import com.cloud.kiidlibrary.dal.KiidRepository;
import com.cloud.kiidlibrary.exceptions.NotFoundException;
import com.cloud.kiidlibrary.model.Kiid;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.TestClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@TestPropertySource(properties = { "tesseract.dataPath=src/test/java/com/cloud/kiidlibrary/resources/tessdata" })
//@TestPropertySource(locations="../src/test/java/com/cloud/kiidlibrary/resources:bootstrap.properties")
@ActiveProfiles("junit")
public class KiidControllerTest {

    @Autowired
    private KiidController controller;
    @Autowired
    private KiidRepository kiidRepository;

    private CountDownLatch lock = new CountDownLatch(1);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void getAllKiids() throws IOException, InterruptedException {
        assertTrue(controller.getAllKiids().size() > 0);
    }

    @Test
    public void uploadPNGFileOCR() throws IOException, InterruptedException {
        //UPLOAD A FILE
        String fileName = "kiid_file.png";
        Path resourceDirectory = Paths.get("src", "test", "java", "com", "cloud", "kiidlibrary", "resources", "kiid_file.png");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = resourceDirectory.toFile();
        String absolutePath = file.getAbsolutePath();
        String contentType = "multipart/form-data";
        byte[] content = null;
        try {
            content = Files.readAllBytes(resourceDirectory);
        } catch (final IOException e) {
        }
        MultipartFile multipartFile = new MockMultipartFile(fileName,
                fileName, contentType, content);
        Kiid kiidUploaded = controller.uploadFile(multipartFile);
        lock.await(20000, TimeUnit.MILLISECONDS);

        // TEST CONTROLLER: getAllKiidSettings
        Map<String, String> kiidProperties = (Map<String, String>) controller.getAllKiidSettings(kiidUploaded.getId().toString());
        assertEquals(46, kiidProperties.size());
        assertEquals(String.valueOf(kiidProperties.get("1;0")), String.valueOf("INVESTORS"));
        assertEquals(String.valueOf(kiidProperties.get("1;20")), String.valueOf("investment schemes managed by AILX as determined by the Remuneration Committee."));
        assertEquals(String.valueOf(kiidProperties.get("1;45")), String.valueOf("AILX Remuneration Policy February 2016"));

        // TEST CONTROLLER: getKiidByCloudId + delete
        String nextCloudId = kiidUploaded.getNextCloudId();
        nextCloudId = nextCloudId.substring(nextCloudId.indexOf("/") + 1, nextCloudId.indexOf("."));
        assertTrue(controller.deleteKiidByCloudId(nextCloudId, fileName.substring(fileName.lastIndexOf('.'))));
    }

    @Test
    public void uploadPDFFileOCR() throws IOException, InterruptedException {
        //UPLOAD A FILE
        String fileName = "kiid_file_ocr.pdf";
        Path resourceDirectory = Paths.get("src", "test", "java", "com", "cloud", "kiidlibrary", "resources", "kiid_file_ocr.pdf");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = resourceDirectory.toFile();
        String absolutePath = file.getAbsolutePath();
        String contentType = "multipart/form-data";
        byte[] content = null;
        try {
            content = Files.readAllBytes(resourceDirectory);
        } catch (final IOException e) {
        }
        MultipartFile multipartFile = new MockMultipartFile(fileName,
                fileName, contentType, content);
        Kiid kiidUploaded = controller.uploadFile(multipartFile);
        lock.await(20000, TimeUnit.MILLISECONDS);

        // TEST CONTROLLER: getAllKiidSettings
        Map<String, String> kiidProperties = (Map<String, String>) controller.getAllKiidSettings(kiidUploaded.getId().toString());
        assertEquals(46, kiidProperties.size());
        assertEquals(String.valueOf(kiidProperties.get("1;0")), String.valueOf("INVESTORS"));
        assertEquals(String.valueOf(kiidProperties.get("1;20")), String.valueOf("investment schemes managed by AILX as determined by the Remuneration Committee."));
        assertEquals(String.valueOf(kiidProperties.get("1;45")), String.valueOf("AILX Remuneration Policy February 2016"));

        // TEST CONTROLLER: getKiidByCloudId + delete
        String nextCloudId = kiidUploaded.getNextCloudId();
        nextCloudId = nextCloudId.substring(nextCloudId.indexOf("/") + 1, nextCloudId.indexOf("."));
        assertTrue(controller.deleteKiidByCloudId(nextCloudId,""));

        Kiid kiidToDelete = controller.getKiidByCloudId(nextCloudId);
        assertTrue(kiidToDelete.getDeleted());
        kiidRepository.delete(kiidToDelete);
        try {
            kiidToDelete = controller.getKiidByCloudId(nextCloudId);
        } catch (NotFoundException e) {
            // SUCCESSFULLY DELETED
        }
    }

    @Test
    public void uploadFile() throws IOException, InterruptedException {
        //UPLOAD A FILE
        String fileName = "kiid_file.pdf";
        Path resourceDirectory = Paths.get("src", "test", "java", "com", "cloud", "kiidlibrary", "resources", "kiid_file.pdf");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = resourceDirectory.toFile();
        String absolutePath = file.getAbsolutePath();
        String contentType = "multipart/form-data";
        byte[] content = null;
        try {
            content = Files.readAllBytes(resourceDirectory);
        } catch (final IOException e) {
        }
        MultipartFile multipartFile = new MockMultipartFile(fileName,
                fileName, contentType, content);
        Kiid kiidUploaded = controller.uploadFile(multipartFile);
        lock.await(5000, TimeUnit.MILLISECONDS);

        //BUILD THE EXPECTED DOCUMENT
        byte[] fileBytes = multipartFile.getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        PDDocument doc = PDDocument.load(fileBytes);
        PDDocumentInformation info = doc.getDocumentInformation();
        String properties = info.getKeywords();
        Kiid kiidExpected = new Kiid(kiidUploaded.getNextCloudId(), info, multipartFile.getOriginalFilename(), Kiid.convertProperties(info.getKeywords()));

        //COMPARE
        this.compareObject(kiidUploaded, kiidExpected);

        // TEST CONTROLLER: getKiid
        kiidUploaded = controller.getKiid(kiidUploaded.getId().toString()).get();
        this.compareObject(kiidUploaded, kiidExpected);

        // TEST CONTROLLER: getAllKiidSettings
        Map<String, String> kiidProperties = (Map<String, String>) controller.getAllKiidSettings(kiidUploaded.getId().toString());
        assertTrue(kiidProperties.size() == kiidUploaded.getKiidProperties().size());
        assertEquals(String.valueOf(kiidProperties.get("ShareClass")), String.valueOf(kiidUploaded.getKiidProperties().get("ShareClass")));

        // TEST CONTROLLER: getKiidProperty
        String kiidProperty = controller.getKiidProperty(kiidUploaded.getId().toString(), "ShareClass");
        assertEquals(String.valueOf(kiidProperty), String.valueOf(kiidUploaded.getKiidProperties().get("ShareClass")));

        // TEST CONTROLLER: getAllKiidSettings
        controller.addKiidProperty(kiidUploaded.getId().toString(), "SEDOL", "ELENA");
        kiidProperty = controller.getKiidProperty(kiidUploaded.getId().toString(), "SEDOL");
        assertEquals(String.valueOf("ELENA"), String.valueOf(kiidProperty));

        // TEST CONTROLLER: getKiidByCloudId + delete
        String nextCloudId = kiidUploaded.getNextCloudId();
        nextCloudId = nextCloudId.substring(nextCloudId.indexOf("/") + 1, nextCloudId.indexOf("."));
        assertTrue(controller.deleteKiidByCloudId(nextCloudId, ""));

        Kiid kiidToDelete = controller.getKiidByCloudId(nextCloudId);
        assertTrue(kiidToDelete.getDeleted());
        kiidRepository.delete(kiidToDelete);
        try {
            kiidToDelete = controller.getKiidByCloudId(nextCloudId);
        } catch (NotFoundException e) {
            // SUCCESSFULLY DELETED
        }

        assertTrue(absolutePath.endsWith("kiid_file.pdf"));
    }

    private Boolean compareObject(Kiid kiidUploaded, Kiid kiidExpected) {
        Class<?> c = kiidUploaded.getClass();
        Method[] fields = c.getDeclaredMethods();
        for (Method field : fields) {
            try {
                if (field.getName().toLowerCase().startsWith("get")) {
                    System.out.println("Comparing:" + field.getName());
                    if (!(field.invoke(kiidUploaded) == null || field.invoke(kiidExpected) == null) && field.getName() != "getCreationDate" && field.getName() != "getUpdateDate") {
                        System.out.println(field.getName() + ": " + field.invoke(kiidUploaded) + " == " + field.invoke(kiidExpected));
                        System.out.println("diff is :" + StringUtils.difference(field.invoke(kiidUploaded).toString(), field.invoke(kiidExpected).toString()));
                        String diff = StringUtils.difference(field.invoke(kiidUploaded).toString(), field.invoke(kiidExpected).toString());
                        assertTrue(diff.isEmpty());
                    }
                }
            } catch (IllegalArgumentException e1) {
            } catch (IllegalAccessException e1) {
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}