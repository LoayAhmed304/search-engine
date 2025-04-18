package com.project.searchengine.server.config;

import com.project.searchengine.server.model.InvertedIndex;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.*;

@Configuration
public class MongoConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        IndexOperations indexOperations = mongoTemplate.indexOps(InvertedIndex.class);

        // Create a unique index on the "word" field
        indexOperations.ensureIndex(
            new Index().on("word", Sort.Direction.ASC).unique().named("word_unique_index")
        );

        System.out.println("MongoDB index created for InvertedIndex collection.");
    }
}
