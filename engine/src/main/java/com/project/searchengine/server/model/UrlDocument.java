package com.project.searchengine.server.model;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urlsfrontier")
@CompoundIndexes({
    @CompoundIndex(name = "isCrawled_frequency_idx", def = "{'isCrawled': 1, 'frequency': -1}")
})
public class UrlDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String normalizedUrl;
    
    private long frequency;
    private boolean isCrawled;
    private boolean isIndexed;
    private String document; // raw HTML content
    private String hashedDocContent;
    private List<String> linkedPages;
    private String lastCrawled;
    private double rank;

    @Autowired
    public UrlDocument() {
        this.linkedPages = new ArrayList<>();
    }

    public UrlDocument(
        String normalizedUrl,
        long frequency,
        boolean isCrawled,
        String document,
        String hashedDocContent,
        List<String> linkedPages,
        String lastCrawled
    ) {
        this.normalizedUrl = normalizedUrl;
        this.frequency = frequency;
        this.isCrawled = isCrawled;
        this.document = document;
        this.hashedDocContent = hashedDocContent;
        this.linkedPages = linkedPages;
        this.lastCrawled = lastCrawled;
        this.rank = 0.0;
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

    public boolean isIndexed() {
        return isIndexed;
    }

    public void setIndexed(boolean indexed) {
        isIndexed = indexed;
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

    public String getLastCrawled() {
        return lastCrawled;
    }

    public void setLastCrawled(String lastCrawled) {
        this.lastCrawled = lastCrawled;
    }

    @Override
    public String toString() {
        return (
            "UrlDocument{" +
            "id='" +
            id +
            '\'' +
            ", normalizedUrl='" +
            normalizedUrl +
            '\'' +
            ", frequency=" +
            frequency +
            ", isCrawled=" +
            isCrawled +
            ", document='" +
            document +
            '\'' +
            ", hashedDocContent='" +
            hashedDocContent +
            '\'' +
            ", linkedPages=" +
            linkedPages +
            ", lastCrawled=" +
            lastCrawled +
            '}'
        );
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }
}
