package com.project.searchengine.server.model;

import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "urlsfrontier")
public class UrlsFrontier {

    @Id
    private String id;

    private String normalizedUrl;
    private long frequency;
    private boolean isCrawled;
    private String document; // raw HTML content
    private String hashedDocContent;
    private List<String> linkedPages;
    private Date lastCrawled;

    public UrlsFrontier(String normalizedUrl, long frequency, boolean isCrawled,
            String document, String hashedDocContent,
            List<String> linkedPages, Date lastCrawled) {
        this.normalizedUrl = normalizedUrl;
        this.frequency = frequency;
        this.isCrawled = isCrawled;
        this.document = document;
        this.hashedDocContent = hashedDocContent;
        this.linkedPages = linkedPages;
        this.lastCrawled = lastCrawled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNormalizedUrl() {
        return normalizedUrl;
    }

    public void setNormalizedUrl(String normalizedUrl) {
        this.normalizedUrl = normalizedUrl;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public boolean isCrawled() {
        return isCrawled;
    }

    public void setCrawled(boolean crawled) {
        isCrawled = crawled;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getHashedDocContent() {
        return hashedDocContent;
    }

    public void setHashedDocContent(String hashedDocContent) {
        this.hashedDocContent = hashedDocContent;
    }

    public List<String> getLinkedPages() {
        return linkedPages;
    }

    public void setLinkedPages(List<String> linkedPages) {
        this.linkedPages = linkedPages;
    }

    public Date getLastCrawled() {
        return lastCrawled;
    }

    public void setLastCrawled(Date lastCrawled) {
        this.lastCrawled = lastCrawled;
    }

    @Override
    public String toString() {
        return "UrlDocument{" +
                "id='" + id + '\'' +
                ", normalizedUrl='" + normalizedUrl + '\'' +
                ", frequency=" + frequency +
                ", isCrawled=" + isCrawled +
                ", document='" + document + '\'' +
                ", hashedDocContent='" + hashedDocContent + '\'' +
                ", linkedPages=" + linkedPages +
                ", lastCrawled=" + lastCrawled +
                '}';
    }
}
