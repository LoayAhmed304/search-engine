package com.project.searchengine.queryprocessor;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.indexer.StopWordFilter;
import com.project.searchengine.server.model.PageReference;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.*;

import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.QueryService;

@Component
public class QueryProcessor {
    private final QueryService queryService;

    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer = new PorterStemmer();

    SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    private Map<String, String> processedWordToOriginal;

    public QueryProcessor(StopWordFilter stopWordFilter, QueryService queryService) {
        this.stopWordFilter = stopWordFilter;
        this.queryService = queryService;
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
    public List<String> processQuery(String query) {
        List<String> processedQuery = new ArrayList<>();

        query = query.toLowerCase();
        String tokens[] = tokenizer.tokenize(query);

        for (String token : tokens) {
            String originalWord = token;

            if (stopWordFilter.isStopWord(token)) {
                continue;
            }

            token = stemmer.stem(token);
            token = token.replaceAll("[^a-z]", "");

            processedWordToOriginal.put(originalWord, token);

            if (!token.isEmpty()) {
                processedQuery.add(token);
            }
        }

        return processedQuery;
    }

    private List<PageReference> retrieveTokenPages(String token) {
        return queryService.getTokenPages(token);
    }

    public Map<String, List<PageReference>> retrieveQueryPages(List<String> processedQuery) {
        Map<String, List<PageReference>> queryPages = new HashMap<>();

        for (String token : processedQuery) {
            List<PageReference> tokenPages = retrieveTokenPages(token);
            queryPages.put(token, tokenPages);
        }
        return queryPages;
    }

    public boolean isPhraseMatchQuery(String query) {
        if (query.isEmpty() || query.length() < 2) {
            return false;
        }

        return (query.charAt(0) == '\"' && query.charAt(query.length() - 1) == '\"')
                ? true
                : false;
    }

    public void run() {
        String testQuery = "test";
        List<String> processedQuery = processQuery(testQuery);
        Map<String, List<PageReference>> queryPage = retrieveQueryPages(processedQuery);


        // filter out results if 

            for(String token : sortedTokens) {
                // get word positions 
                for(pageReference : pagereferences list) {

                    // List<Integer> tokenPositions = PageReference.getWordPositions(token);
    
                    for(Integer pos : tokenPositions) {
                        // if at position don't match original skip 
                        // if it does check the rest of the tokens 
                        // i need another map for stemmed to map to original again :)
                    }

                    // check prev positions 
                    // get position of token in original qury 
                    // check all letters before it match in original setence
                    // check next positions 
                    // check all letters after it match in original sentence 
                }
            }
        }
    }
}
