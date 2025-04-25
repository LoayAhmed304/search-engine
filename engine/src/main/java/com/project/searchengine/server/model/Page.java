package com.project.searchengine.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pages")
public class Page {

    @Id
    private String id;

    private String url;
    private String title;
    private String content;
    private int pageTokenCount;
    private double rank;

    public Page(String id, String url, String title, String content, int pageTokenCount) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.content = content;
        this.rank = 0.0;
        this.pageTokenCount = pageTokenCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public int getPageTokenCount(int pageTokenCount) {
        return pageTokenCount;
    }

    public void setPageTokenCount(int pageTokenCount) {
        this.pageTokenCount = pageTokenCount;
    }

    @Override
    public String toString() {
        return (
            "Page{" +
            "id='" +
            id +
            '\'' +
            ", url='" +
            url +
            '\'' +
            ", title='" +
            title +
            '\'' +
            ", content='" +
            content +
            '\'' +
            '}'
        );
    }
}
