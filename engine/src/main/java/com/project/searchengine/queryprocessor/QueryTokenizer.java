package com.project.searchengine.queryprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.indexer.StopWordFilter;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class QueryTokenizer {

    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer = new PorterStemmer();
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    @Autowired
    private PhraseMatcher phraseMatcher;

    public QueryTokenizer(StopWordFilter stopWordFilter) {
        this.stopWordFilter = stopWordFilter;
    }
   
    /**
     * Query processing matches the indexer to ensure queries & indexed documents
     * match
     * 
     * 1. converts query to lower case
     * 2. tokenizes query using OpenNLP's SimpleTokenizer
     * 3. Removes stop words using StopWordFilter
     * 4. porter stemming on each token
     * 5. removes any remaining non-alphabetics
     * 
     * @param query: the search query itself
     * @return List of cleaned tokens ready for search
     */
    public QueryResult tokenizeQuery(String query) {
        List<String> tokenizedQuery = new ArrayList<>();
        Map<String, String> tokenizedToOriginal = new HashMap<>();
        
        query = query.toLowerCase();
        String tokens[] = tokenizer.tokenize(query);
        List<String> originalWords = new ArrayList<>(Arrays.asList(tokens));
        
        boolean isPhraseMatch = phraseMatcher.isPhraseMatchQuery(query);

        if (isPhraseMatch) {
            originalWords.remove(0);
            originalWords.remove(originalWords.size() - 1);
        }
        

        for (String token : tokens) {
            String originalWord = token;

            if (stopWordFilter.isStopWord(token)) {
                continue;
            }

            token = stemmer.stem(token);
            token = token.replaceAll("[^a-z]", "");

            tokenizedToOriginal.put(token, originalWord);

            if (!token.isEmpty()) {
                tokenizedQuery.add(token);
            }
        }

        return new QueryResult(tokenizedQuery, tokenizedToOriginal, originalWords, isPhraseMatch);
    }
}
