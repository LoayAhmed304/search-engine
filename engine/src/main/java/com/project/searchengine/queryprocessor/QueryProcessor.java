package com.project.searchengine.queryprocessor;

import java.util.*;

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
    private List<String> query;


    private final QueryService queryService;

    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer = new PorterStemmer();

    SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

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
            if (stopWordFilter.isStopWord(token)) {
                continue;
            }

            token = stemmer.stem(token);
            token = token.replaceAll("[^a-z]", "");

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

        for(String token : processedQuery) {
            List<PageReference> tokenPages = retrieveTokenPages(token);
            queryPages.put(token, tokenPages);
        }
        return queryPages;
    }
}
