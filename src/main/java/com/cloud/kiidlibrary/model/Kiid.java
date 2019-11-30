package com.cloud.kiidlibrary.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Kiid {

    @Id
    private String kiidId;
    private String title;
    private String author;
    private String subject;
    private String fileName;
    private Date creationDate = new Date();
    private Date updateDate = new Date();
    private Map<String, String> KiidProperties = new HashMap<>();

    public String getKiidId() {
        return kiidId;
    }

    public void setKiidId(String kiidId) {
        this.kiidId = kiidId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Map<String, String> getKiidProperties() {
        return KiidProperties;
    }

    public void setKiidProperties(Map<String, String> kiidProperties) {
        KiidProperties = kiidProperties;
    }
}