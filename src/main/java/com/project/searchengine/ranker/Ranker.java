package com.project.searchengine.ranker;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageService;
import java.util.*;

public class Ranker {

    private final List<String> queryTokens;
    private final Map<String, List<PageReference>> wordResults;
    private final long totalDocuments;
    private final PageService pageService;

    public Ranker(String query, Map<String, List<PageReference>> wordResults) {
        this.queryTokens = Arrays.asList(query.split("\\s+"));
        this.wordResults = wordResults;
        this.pageService = new PageService();
        this.totalDocuments = pageService.getTotalDocuments();
    }

    /**
     * The main function, which handles the ranking for all the given results
     *
     * @return ordered ranked list of the results pages IDs
     */
    public List<String> rank() {
        Map<String, Double> scores = new HashMap<>();
        for (String token : queryTokens) {
            processToken(token, scores);
        }

        return sortedPages(scores); // return sorted pages ids (strings) according to their values in scores Map
    }

    /**
     * Processes a single token's score
     *
     * @param token: the token (word) desired to process
     * @param scores: Map object by reference, to update the total score of every page (<pageId, score>)
     */
    private void processToken(String token, Map<String, Double> scores) {
        List<PageReference> prs = wordResults.getOrDefault(token, Collections.emptyList());

        double idf = getIDF(prs.size());
        for (PageReference pr : prs) {
            double pageRank = pr.getPageRank();

            double tf = RankCalculator.calculateTF(pr);
            double score = RankCalculator.calculateScore(tf, idf, pageRank);

            String pageId = pr.getPageId();
            scores.merge(pageId, score, Double::sum);
        }
    }

    /**
     * Simple computation for the normalized IDF value
     *
     * @param docsWithToken: number of documents contianing the token
     * @return normalized IDF value (double)
     */
    private double getIDF(int docsWithToken) {
        return Math.log((double) docsWithToken / totalDocuments);
    }

    /**
     * Creates the sorted pages IDs according to their score in the scores Map <page ID, score>
     *
     * @param scores: Map of score corresponding to each page
     * @return sorted array of page IDs ([String, ...])
     */
    private List<String> sortedPages(Map<String, Double> scores) {
        List<String> result = new ArrayList<>(scores.keySet());
        result.sort((page1, page2) -> Double.compare(scores.get(page2), scores.get(page1)));
        return result;
    }
}
