package com.project.searchengine.server.service;

import com.project.searchengine.queryprocessor.QueryProcessor;
import com.project.searchengine.server.dto.QueryResult;
import com.project.searchengine.server.model.Page;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryResultService {

    @Autowired
    private QueryProcessor queryProcessor;

    @Autowired
    private PageService pageService;

    private Map<String, Integer> queryTotalPagesCache = new HashMap<>();

    /**
     * Returns a list of QueryResult objects based on the given query.
     *
     * @param query The search query to process.
     * @return A list of QueryResult objects containing the URL, title, and snippet for each page.
     */
    public List<QueryResult> getQueryResults(String query, int pageNumber) {
        List<QueryResult> results = new ArrayList<>();

        // Get <pageReference, snippet> pairs from query processor
        Map<String, String> snippets = queryProcessor.getAllPagesSnippets(query, pageNumber);

        // Cache the total pages count for this query
        queryTotalPagesCache.put(query, queryProcessor.getTotalPages());

        // Transform to QueryResult objects
        for (Map.Entry<String, String> entry : snippets.entrySet()) {
            String pageId = entry.getKey();
            Page page = pageService.getPage(pageId);
            String snippet = entry.getValue();

            results.add(new QueryResult(page.getUrl(), page.getTitle(), snippet));
        }

        return results;
    }

    public int getTotalPages(String query) {
        // If we've already processed this query, return the cached count
        if (queryTotalPagesCache.containsKey(query)) {
            return queryTotalPagesCache.get(query);
        }

        // If not in cache (shouldn't happen if controller calls getQueryResults first)
        // Process the query at page 0 to get total pages
        queryProcessor.getAllPagesSnippets(query, 0);
        int totalPages = queryProcessor.getTotalPages();
        queryTotalPagesCache.put(query, totalPages);
        return totalPages;
    }
}
