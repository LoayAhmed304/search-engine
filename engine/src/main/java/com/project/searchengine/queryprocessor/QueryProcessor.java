package com.project.searchengine.queryprocessor;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.indexer.StopWordFilter;
import com.project.searchengine.ranker.Ranker;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.*;

@Component
public class QueryProcessor {
    private List<String> query;
    private Ranker ranker;

    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer = new PorterStemmer();

    SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public QueryProcessor(StopWordFilter stopWordFilter, Ranker ranker) {
        this.stopWordFilter = stopWordFilter;
        this.ranker = ranker;
    }

    /**
     * Query processing matches the indexer to ensure queries & indexed documents match
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
    public List<String> processQuery(String query) {
        List<String> processedTokens = new ArrayList<>();

        query = query.toLowerCase();
        String tokens[] = tokenizer.tokenize(query);

        try {

            for (String token : tokens) {
                if (stopWordFilter.isStopWord(token)) {
                    continue;
                }

                token = stemmer.stem(token);
                token.replaceAll("[^a-z]", "");

                if (!token.isEmpty()) {
                    processedTokens.add(token);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing query: " + e.getMessage());
        }
        return processedTokens;
    }
}
