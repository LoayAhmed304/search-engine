package com.project.searchengine.runner;

import com.project.searchengine.indexer.Indexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("indexer")
public class IndexerRunner implements CommandLineRunner {

    @Autowired
    private Indexer indexer;

    @Override
    public void run(String... args) {
        System.out.println("Starting the indexer...");
        long start = System.currentTimeMillis();
        indexer.startIndexing();
        System.out.println("Indexing took: " + (System.currentTimeMillis() - start) + "ms");
    }
}
