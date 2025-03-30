package com.project.searchengine.ranker;

import com.project.searchengine.server.model.PageReference;

public class RankCalculator {

    private static final double TF_IDF_COEFF = 0.7;
    private static final double PAGE_RANK_COEFF = 0.3;

    /**
     * Calculates the weighted Term Frequency
     * TF(weighted) = number of token occurence in document / total tokens in document
     *
     * @param pr: PageReference that the token is part of
     * @return normalized TF value (double)
     */
    public static double calculateTF(PageReference pr) {
        return 1.2;
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
        return 0.3;
    }
}
