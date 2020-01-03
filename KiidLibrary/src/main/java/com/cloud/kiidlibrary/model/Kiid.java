package com.cloud.kiidlibrary.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Kiid {
    @Id
    private ObjectId id;
    private String kiidId;
    private String title;
    private String author;
    private String subject;
    private String fileName;
    private String producer;
    private String creator;
    private Date creationDate = new Date();
    private Date updateDate = new Date();
    private Map<String, String> kiidProperties = new HashMap<>();
    private String nextCloudId;
    private Boolean deleted;

    public Kiid(String nextCloudId, String title, String author, String subject, String fileName, String producer, String creator, Map<String, String> kiidProperties)
    {
        this.nextCloudId = nextCloudId;
        this.title = title;
        this.author = author;
        this.subject = subject;
        this.fileName = fileName;
        this.producer = producer;
        this.creator = creator;
        this.kiidProperties = kiidProperties;
    }

    public Kiid()
    {
    }

    public Kiid(String nextCloudId, String title)
    {
        this.nextCloudId = nextCloudId;
        this.title = title;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getNextCloudId() {
        return nextCloudId;
    }

    public String getUuid() {
        return nextCloudId.substring(nextCloudId.indexOf("/") + 1, nextCloudId.indexOf("."));
    }

    public void setNextCloudId(String nextCloudId) {
        this.nextCloudId = nextCloudId;
    }


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
        return kiidProperties;
    }

    public void setKiidProperties(Map<String, String> kiidProperties) {
        this.kiidProperties = kiidProperties;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public static Map<String, String> convertProperties(String keywords)
    {
        Map<String,String> map = new HashMap<>();
        if (keywords != null && ! keywords.isEmpty()) {
            String[] keyValuePairs = keywords.split(";");
            for(String pair : keyValuePairs)
            {
                String[] entry = pair.split("=");
                map.put(entry[0].trim(), entry[1].trim());
            }
        }
        return  map;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}