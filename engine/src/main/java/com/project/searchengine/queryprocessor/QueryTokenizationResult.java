package com.project.searchengine.queryprocessor;

import java.util.List;
import java.util.Map;

public class QueryTokenizationResult {
    private List<String> tokenizedQuery;
    private Map<String, String> tokenizedToOriginal;
    private List<String> originalWords;
    private boolean isPhraseMatch;

    public QueryTokenizationResult(List<String> processedTokens,
            Map<String, String> tokenizedToOriginalWord,
            List<String> originalWords,
            boolean isPhraseMatch) {

        this.tokenizedQuery = processedTokens;
        this.tokenizedToOriginal = tokenizedToOriginalWord;
        this.originalWords = originalWords;
        this.isPhraseMatch = isPhraseMatch;
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

    public boolean getIsPhraseMatch() {
        return isPhraseMatch;
    }
}
