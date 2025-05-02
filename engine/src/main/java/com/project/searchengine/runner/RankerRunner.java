package com.project.searchengine.runner;

import com.project.searchengine.queryprocessor.QueryProcessor;
import com.project.searchengine.queryprocessor.QueryResult;
import com.project.searchengine.queryprocessor.QueryTokenizer;
import com.project.searchengine.ranker.Ranker;
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
            System.out.println("Processing query: \"" + query + "\"");

            // 1) Tokenize query
            QueryResult tokenizationResult = queryTokenizer.tokenizeQuery(query);
            List<String> tokenizedQuery = tokenizationResult.getTokenizedQuery();
            System.out.println("Tokenized query: " + tokenizedQuery);

            // 2) Retrieve pages for each token
            Map<String, List<PageReference>> result = queryProcessor.retrieveQueryPages(
                tokenizedQuery
            );

            for (Map.Entry<String, List<PageReference>> entry : result.entrySet()) {
                System.out.println(
                    "Token: " + entry.getKey() + ", Pages: " + entry.getValue().size()
                );
            }

            if (result.isEmpty()) {
                System.out.println("Nothing found for the query");
                return;
            }

            // 3) Rank the results
            long startTime = System.currentTimeMillis();
            List<String> rankedResults = ranker.rank(result);
            long endTime = System.currentTimeMillis();

            System.out.println("Results:");
            int count = 1;
            for (String pageId : rankedResults) {
                System.out.println(count + ". PageID: " + pageId);
                count++;
            }
            System.out.println("Ranking time: " + (endTime - startTime) + " ms");
            System.out.println("Ranking completed successfully.");
            System.out.println("Ranked results size: " + rankedResults.size());
        } catch (Exception e) {
            System.err.println("Error in RankerRunner: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
