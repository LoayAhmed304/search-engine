package com.project.searchengine.runner;

import com.project.searchengine.queryprocessor.QueryProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("query")
public class QueryRunner implements CommandLineRunner {

    @Autowired
    private QueryProcessor queryProcessor;

    @Override
    public void run(String... args) {
        System.out.println("Starting the query processor...");
        long start = System.currentTimeMillis();

        // String testQuery = "\"coding problems\"";
        String testQuery = "role";

        queryProcessor.process(testQuery, 0);
        System.out.println("quering took: " + (System.currentTimeMillis() - start) + "ms");
    }
}