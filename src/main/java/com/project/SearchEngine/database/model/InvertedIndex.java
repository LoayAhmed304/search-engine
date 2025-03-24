package com.project.SearchEngine.database.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inverted_index")
public class InvertedIndex {
    private String word;
    
    
}
