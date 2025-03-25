package com.project.SearchEngine.server.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;


@Document(collection = "words")
public class InvertedIndex {
    private String word;
    private List<PageReference> pages;
    private int pageCount; // number of pages containing the word

    public InvertedIndex(String word) {
        this.word = word;
        this.pages = new ArrayList<>();
        this.pageCount = 0;
    }

    @Override
    public String toString() {
        return "InvertedIndex{" +
                "word='" + word + '\'' +
                ", pages=" + pages +
                ", pageCount=" + pageCount +
                '}';
    }
}
