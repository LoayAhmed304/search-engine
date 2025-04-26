package com.project.searchengine.runner;

import com.project.searchengine.ranker.PageRank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("pagerank")
public class PageRankRunner implements CommandLineRunner {

    @Autowired
    private PageRank pageRank;

    @Override
    public void run(String... args) {
        System.out.println("Starting the PageRank algorithm...");
        long start = System.currentTimeMillis();

        pageRank.computeAllRanks();

        System.out.println(
            "PageRank took: " + (System.currentTimeMillis() - start) / 60000 + " minutes"
        );
    }
}
