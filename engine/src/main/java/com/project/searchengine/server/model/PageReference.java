package com.project.searchengine.server.model;

import java.util.*;

public class PageReference {

    private String pageId;

    private List<Integer> wordPositions;
    private Map<String, Integer> fieldWordCount; // field type -> word count

    private int pageTokenCount;
    private double pageRank;

    public PageReference(String pageId, int pageTokenCount, double pageRank) {
        this.pageId = pageId;
        this.wordPositions = new ArrayList<>();
        this.fieldWordCount = new HashMap<>();
        this.pageTokenCount = pageTokenCount;
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

    public int getPageTokenCount() {
        return pageTokenCount;
    }

    public void setPageTokenCount(int pageTokenCount) {
        this.pageTokenCount = pageTokenCount;
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
