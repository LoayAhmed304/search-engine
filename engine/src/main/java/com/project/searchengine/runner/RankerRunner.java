package com.project.searchengine.runner;

import com.project.searchengine.queryprocessor.QueryProcessor;
import com.project.searchengine.queryprocessor.QueryTokenizationResult;
import com.project.searchengine.queryprocessor.QueryTokenizer;
import com.project.searchengine.ranker.Ranker;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ranker")
public class RankerRunner implements CommandLineRunner {

    @Autowired
    private QueryProcessor queryProcessor;

    @Autowired
    private QueryTokenizer queryTokenizer;

    @Autowired
    private Ranker ranker;

    @Override
    public void run(String... args) {
        try {
            String query = "study math"; // Default query
            for (String arg : args) {
                if (!arg.startsWith("--")) {
                    query = arg;
                    break;
                }
            }
            Long totalStartTime = System.currentTimeMillis();
            // 1) Tokenize query
            QueryTokenizationResult tokenizationResult = queryTokenizer.tokenizeQuery(query);
            List<String> tokenizedQuery = tokenizationResult.getTokenizedQuery();
            System.out.println("Tokenized query: " + tokenizedQuery);

            // 2) Retrieve pages for each token
            Map<String, List<PageReference>> results = queryProcessor.retrieveQueryPages(
                tokenizedQuery
            );

            if (results.isEmpty()) {
                System.out.println("Nothing found for the query");
                return;
            }

            // 3) Rank the results
            long startTime = System.currentTimeMillis();
            Map<PageReference, String> rankedResults = ranker.rank(results);
            long endTime = System.currentTimeMillis();

            int count = 1;
            for (PageReference pageRef : rankedResults.keySet()) {
                String pageId = pageRef.getPageId();
                System.out.println("[" + count + "] Page ID: " + pageId);
                count++;
            }

            System.out.println("Ranking time: " + (endTime - startTime) + " ms");
            System.out.println("Ranked results size: " + rankedResults.size());

            long totalEndTime = System.currentTimeMillis();
            System.out.println("Total runner time: " + (totalEndTime - totalStartTime) + " ms");
        } catch (Exception e) {
            System.err.println("Error in RankerRunner: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
