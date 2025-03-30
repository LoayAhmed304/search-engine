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

    public Page(String id, String url, String title, String content) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.content = content;
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
