package com.project.searchengine.server.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "queries")
public class SearchQuery {
    String query;

    public SearchQuery(String query) {
        this.query = query;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
}
