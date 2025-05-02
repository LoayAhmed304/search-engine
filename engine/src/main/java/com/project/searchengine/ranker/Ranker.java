package com.project.searchengine.ranker;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageReferenceService;
import com.project.searchengine.server.service.PageService;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class Ranker {

    private final long totalDocuments;
    private final PageReferenceService pageReferenceService;

    public Ranker(PageService pageService, PageReferenceService pageReferenceService) {
        this.totalDocuments = pageService.getTotalDocuments();
        this.pageReferenceService = pageReferenceService;
    }

    /**
     * * This function takes a map of token to page references
     * and returns an ordered map of page references to their corresponding token (the first matched token)
     *
     * @param queryResults a map of token to page references
     * @return a map of page references to their corresponding token (the first matched token)
     */
    public Map<PageReference, String> rank(Map<String, List<PageReference>> queryResults) {
        Map<String, Double> scores = computeScores(queryResults);
        List<String> sortedPageIds = sortedPages(scores); // get the sorted pages ids according to their values in scores Map

        return getOrderedPageReferences(queryResults, sortedPageIds);
    }

    /**
     * Main for loop to process each token and compute pages scores Map
     *
     * @return Map with page_id as its key, and page score as its value
     */
    Map<String, Double> computeScores(Map<String, List<PageReference>> queryResults) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Double> pagesRanks = getPagesRanks(queryResults);

        for (List<PageReference> prs : queryResults.values()) {
            processToken(prs, scores, pagesRanks);
        }

        return scores; // return sorted pages ids (strings) according to their values in scores Map
    }

    /**
     * Processes a single token's score
     *
     * @param token: the token (word) desired to process
     * @param scores: Map object by reference, to update the total score of every page (<pageId, score>)
     */
    void processToken(
        List<PageReference> prs,
        Map<String, Double> scores,
        Map<String, Double> pagesRanks
    ) {
        double idf = RankCalculator.getIDF(totalDocuments, prs.size());
        for (PageReference pr : prs) {
            String pageId = pr.getPageId();

            double pageRank = pagesRanks.getOrDefault(pageId, 0.0);

            double tf = pr.getTf();
            double score = RankCalculator.calculateScore(tf, idf, pageRank);
            Map<String, Integer> fieldWordCount = pr.getFieldWordCount();
            double multiplier = 1.0;

            multiplier += 1.0 * Math.log(1.0 + fieldWordCount.getOrDefault("title", 0));
            multiplier += 0.5 * Math.log(1.0 + fieldWordCount.getOrDefault("h1", 0));
            multiplier += 0.25 * Math.log(1.0 + fieldWordCount.getOrDefault("h2", 0));

            // penalize pages without h1
            if (fieldWordCount.getOrDefault("h1", 0).equals(0)) {
                multiplier *= 0.6;
            }

            // cap the multiplier at 10x boost
            multiplier = Math.min(multiplier, 10.0);
            score *= multiplier;
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

    Map<String, Double> getPagesRanks(Map<String, List<PageReference>> queryResults) {
        Set<String> pageIds = new HashSet<>();
        for (List<PageReference> prs : queryResults.values()) {
            for (PageReference pr : prs) {
                pageIds.add(pr.getPageId());
            }
        }

        return pageReferenceService.getPagesRanks(new ArrayList<>(pageIds));
    }

    /**
     * * This function takes a map of token to page references and a list of ordered page IDs,
     * @param tokenToPagesMap a map of token to page references
     * @param orderedPageIds  a list of ordered page IDs
     * @return a map of page references to their corresponding token (the first matched token),
     *         where the page references are ordered according to the orderedPageIds list.
     */

    public static Map<PageReference, String> getOrderedPageReferences(
        Map<String, List<PageReference>> tokenToPagesMap,
        List<String> orderedPageIds
    ) {
        Map<PageReference, String> result = new LinkedHashMap<>();

        for (String pageId : orderedPageIds) {
            boolean found = false;
            for (Map.Entry<String, List<PageReference>> entry : tokenToPagesMap.entrySet()) {
                String token = entry.getKey();
                List<PageReference> pageRefs = entry.getValue();

                for (PageReference pageRef : pageRefs) {
                    if (pageRef.getPageId().equals(pageId)) {
                        result.put(pageRef, token);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }

        return result;
    }
}
