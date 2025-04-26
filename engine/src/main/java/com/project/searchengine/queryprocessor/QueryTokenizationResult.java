package com.project.searchengine.queryprocessor;

import java.util.List;
import java.util.Map;

public class QueryTokenizationResult {
    private List<String> tokenizedQuery;
    private Map<String, String> tokenizedToOriginal;
    private List<String> originalWords;

    public QueryTokenizationResult(List<String> processedTokens,
            Map<String, String> tokenizedToOriginalWord,
            List<String> originalWords) {

        this.tokenizedQuery = processedTokens;
        this.tokenizedToOriginal = tokenizedToOriginalWord;
        this.originalWords = originalWords;
    }

    public List<String> getTokenizedQuery() {
        return tokenizedQuery;
    }

    public Map<String, String> getTokenizedToOriginal() {
        return tokenizedToOriginal;
    }

    public List<String> getOriginalWords() {
        return originalWords;
    }
}
