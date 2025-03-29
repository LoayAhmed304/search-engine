package com.project.searchengine.server.model;

import java.util.*;

public class PageReference {

    private String pageId;
    private List<Integer> wordPositions;
    private Map<String, Integer> fieldWordCount; // field name -> word count

    public PageReference(String pageId) {
        this.pageId = pageId;
        this.wordPositions = new ArrayList<>();
        this.fieldWordCount = new HashMap<>();
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
