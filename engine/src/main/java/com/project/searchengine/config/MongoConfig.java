package com.project.searchengine.config;

import com.project.searchengine.server.model.InvertedIndex;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
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
        IndexOperations indexOps = mongoTemplate.indexOps(InvertedIndex.class);

        try {
            indexOps.ensureIndex(
                new Index().on("word", Sort.Direction.ASC).unique().named("word_unique_index")
            );

            indexOps.ensureIndex(
                new CompoundIndexDefinition(new Document("word", 1).append("pages.pageId", 1))
                    .unique()
                    .named("word_pageId_unique_index")
            );
            System.out.println("MongoDB index created for InvertedIndex collection.");
        } catch (Exception e) {
            System.out.println("Failed to create MongoDB index: " + e.getMessage());
        }
    }
}
