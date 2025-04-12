package com.project.searchengine.server.model;

import java.util.*;

public class PageReference {

    private String pageId;
    private List<Integer> wordPositions;
    private Map<String, Integer> fieldWordCount; // field name -> word count

    private int pageTokens;
    private double pageRank;

    public PageReference(String pageId, int pageTokens, double pageRank) {
        this.pageId = pageId;
        this.wordPositions = new ArrayList<>();
        this.fieldWordCount = new HashMap<>();
        this.pageTokens = pageTokens;
        this.pageRank = pageRank;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public List<Integer> getWordPositions() {
        return wordPositions;
    }

    public void setWordPositions(List<Integer> wordPositions) {
        this.wordPositions = wordPositions;
    }

    public Map<String, Integer> getFieldWordCount() {
        return fieldWordCount;
    }

    public void setfieldWordCount(Map<String, Integer> fieldWordCount) {
        this.fieldWordCount = fieldWordCount;
    }

    public void addWordPosition(int position) {
        wordPositions.add(position);
    }

    public void addfieldWordCount(String field, int count) {
        fieldWordCount.put(field, count);
    }

    public double getPageRank() {
        return pageRank;
    }

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }

    public int getPageTokens() {
        return pageTokens;
    }

    public void setPageTokens(int pageTokens) {
        this.pageTokens = pageTokens;
    }

    @Override
    public String toString() {
        return (
            "PageReference{" +
            "pageId='" +
            pageId +
            '\'' +
            ", wordPositions=" +
            wordPositions +
            ", fieldWordCount=" +
            fieldWordCount +
            '}'
        );
    }
}
