package com.project.searchengine.ranker;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;
import com.project.searchengine.server.service.PageService;
import java.util.*;

public class RankCalculator {

    private static final double TF_IDF_COEFF = 0.7;
    private static final double PAGE_RANK_COEFF = 0.3;

    /**
     * Calculates the weighted Term Frequency and saves it in the PageReference object
     * TF(weighted) = number of token occurence in document / total tokens in document
     *
     * @param indexBuffer: Inverted index buffer
     */
    public static void calculateTf(
        Map<String, InvertedIndex> indexBuffer,
        Map<String, Integer> pageTokenCount
    ) {
        for (InvertedIndex index : indexBuffer.values()) {
            for (PageReference pageReference : index.getPages()) {
                // Calculate TF for each page reference
                String pageId = pageReference.getPageId();
                double tf =
                    (double) pageReference.getWordPositions().size() /
                    pageTokenCount.getOrDefault(pageId, 1);

                // Update the page reference with the calculated TF
                pageReference.setTf(tf);
            }
        }
    }

    /**
     * Calculates the final score to be used for ranking
     * Score = (weight) * TF * IDF + (1-weight) * page rank
     *
     * @param tf: weighted term frequency
     * @param idf: normalized IDF
     * @param pageRank: rank of the page relative to other pages
     * @return the final score to be merged with scores Map (double)
     */
    public static double calculateScore(double tf, double idf, double pageRank) {
        return TF_IDF_COEFF * tf * idf + PAGE_RANK_COEFF * pageRank;
    }

    /**
     * Simple computation for the normalized IDF value
     *
     * @param docsWithToken: number of documents containing the token
     * @return normalized IDF value (double)
     */
    public static double getIDF(long totalDocumentsCount, int docsWithToken) {
        return Math.log((double) totalDocumentsCount / docsWithToken);
    }
}
