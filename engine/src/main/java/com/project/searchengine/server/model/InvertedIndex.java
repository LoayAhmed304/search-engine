package com.project.searchengine.server.model;

import java.util.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "words")
public class InvertedIndex {

    private String word;
    private double idf;
    private List<PageReference> pages;

    public InvertedIndex(String word) {
        this.word = word;
        this.pages = new ArrayList<>();
        idf = 0.0;
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
    }

    public int getPageCount() {
        return this.pages.size();
    }

    public void addPage(PageReference page) {
        pages.add(page);
    }

    @Override
    public String toString() {
        return (
            "InvertedIndex{" +
            "word='" +
            word +
            '\'' +
            ", pages=" +
            pages +
            ", pageCount=" +
            pages.size() +
            '}'
        );
    }
}
