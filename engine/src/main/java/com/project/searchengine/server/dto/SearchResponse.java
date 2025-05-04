package com.project.searchengine.server.dto;

import java.util.List;

public class SearchResponse {
    private List<QueryResult> results;
    private int totalPages;
    private int currentPage;

    public SearchResponse(List<QueryResult> results, int totalPages, int currentPage) {
        this.results = results;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public List<QueryResult> getResults() {
        return results;
    }

    public void setResults(List<QueryResult> results) {
        this.results = results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}