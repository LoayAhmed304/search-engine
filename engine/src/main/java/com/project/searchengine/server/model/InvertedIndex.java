package com.project.searchengine.server.model;

import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "words")
public class InvertedIndex {

    @Id
    private String word;

    private List<PageReference> pages;
    private int pageCount; // number of pages containing the word

    public InvertedIndex(String word) {
        this.word = word;
        this.pages = new ArrayList<>();
        this.pageCount = 0;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<PageReference> getPages() {
        return pages;
    }

    public void setPages(List<PageReference> pages) {
        this.pages = pages;
        this.pageCount = pages.size();
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void addPage(PageReference page) {
        pages.add(page);
        pageCount++;
    }

    @Override
    public String toString() {
        return ("InvertedIndex{" +
                "word='" +
                word +
                '\'' +
                ", pages=" +
                pages +
                ", pageCount=" +
                pageCount +
                '}');
    }
}