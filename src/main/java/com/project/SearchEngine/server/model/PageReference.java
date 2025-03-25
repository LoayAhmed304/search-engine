package com.project.SearchEngine.server.model;

import java.util.*;

public class PageReference {

    // Field names    
    public enum wordField {
        TITLE,
        H1,
        H2,
        H3,
        BODY,
        URL
    }

    private String pageId;
    private List<Integer> wordPositions;
    private Map<String, Integer> wordCount; // field name -> word count

    public PageReference(String pageId) {
        this.pageId = pageId;
        this.wordPositions = new ArrayList<>();
        this.wordCount = new HashMap<>();
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

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    public void setWordCount(Map<String, Integer> wordCount) {
        this.wordCount = wordCount;
    }

    public void addWordPosition(int position) {
        wordPositions.add(position);
    }

    public void addWordCount(String field, int count) {
        wordCount.put(field, count);
    }

    @Override
    public String toString() {
        return "PageReference{" +
                "pageId='" + pageId + '\'' +
                ", wordPositions=" + wordPositions +
                ", wordCount=" + wordCount +
                '}';
    }
}
