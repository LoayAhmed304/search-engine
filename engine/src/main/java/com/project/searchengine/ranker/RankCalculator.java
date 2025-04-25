package com.project.searchengine.ranker;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;
import java.util.*;

public class RankCalculator {

    private static final double TF_IDF_COEFF = 0.7;
    private static final double PAGE_RANK_COEFF = 0.3;

    /**
     * Calculates the weighted Term Frequency and saves it in the PageReference object
     * TF(weighted) = number of token occurence in document / total tokens in document
     *
     * @param pageReference: PageReference that the token is part of
     */
    public static void calculateTf(Map<String, InvertedIndex> indexBuffer) {
        for (InvertedIndex index : indexBuffer.values()) {
            for (PageReference pageReference : index.getPages()) {
                // Calculate TF for each page reference
                double tf =
                    (double) pageReference.getWordPositions().size() /
                    (double) pageReference.getPageTokenCount();

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
}
