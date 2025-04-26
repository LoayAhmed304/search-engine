package com.project.searchengine.ranker;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageReferenceService;
import com.project.searchengine.server.service.PageService;
import java.util.*;

public class Ranker {

    private final List<String> queryTokens;
    private final Map<String, List<PageReference>> queryResults;
    private final long totalDocuments;
    private final PageService pageService;
    private final PageReferenceService pageReferenceService;

    public Ranker(
        String query,
        Map<String, List<PageReference>> queryResults,
        PageService pageService,
        PageReferenceService pageReferenceService
    ) {
        this.queryTokens = Arrays.asList(query.split("\\s+"));
        this.queryResults = queryResults;
        this.pageService = pageService;
        this.totalDocuments = pageService.getTotalDocuments();
        this.pageReferenceService = pageReferenceService;
    }

    /**
     * The main function, which handles the ranking for all the given results
     *
     * @return ranked list of the results pages IDs [String, ...]
     */
    public List<String> rank() {
        Map<String, Double> scores = computeScores();

        return sortedPages(scores); // return sorted pages ids (strings) according to their values in scores Map
    }

    /**
     * Main for loop to process each token and compute pages scores Map
     *
     * @return Map with page_id as its key, and page score as its value
     */
    Map<String, Double> computeScores() {
        Map<String, Double> scores = new HashMap<>();

        for (String token : queryResults.keySet()) {
            processToken(token, scores);
        }

        return scores; // return sorted pages ids (strings) according to their values in scores Map
    }

    /**
     * Processes a single token's score
     *
     * @param token: the token (word) desired to process
     * @param scores: Map object by reference, to update the total score of every page (<pageId, score>)
     */
    void processToken(String token, Map<String, Double> scores) {
        List<PageReference> prs = queryResults.get(token);

        double idf = RankCalculator.getIDF(totalDocuments, prs.size());
        for (PageReference pr : prs) {
            double pageRank = this.pageReferenceService.getPageRank(pr.getPageId());

            double tf = pr.getTf();
            double score = RankCalculator.calculateScore(tf, idf, pageRank);

            String pageId = pr.getPageId();
            scores.merge(pageId, score, Double::sum);
        }
    }

    /**
     * Creates the sorted pages IDs according to their score in the scores Map <page ID, score>
     *
     * @param scores: Map of score corresponding to each page
     * @return sorted array of page IDs ([String, ...])
     */
    List<String> sortedPages(Map<String, Double> scores) {
        List<String> result = new ArrayList<>(scores.keySet());
        result.sort((page1, page2) -> Double.compare(scores.get(page2), scores.get(page1)));
        return result;
    }
}
