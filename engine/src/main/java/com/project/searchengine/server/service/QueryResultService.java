package com.project.searchengine.server.service;

import com.project.searchengine.queryprocessor.QueryProcessor;
import com.project.searchengine.server.dto.QueryResult;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryResultService {

    @Autowired
    private QueryProcessor queryProcessor;

    @Autowired
    private PageService pageService;

    /**
     * Returns a list of QueryResult objects based on the given query.
     *
     * @param query The search query to process.
     * @return A list of QueryResult objects containing the URL, title, and snippet for each page.
     */
    public List<QueryResult> getQueryResults(String query) {
        List<QueryResult> results = new ArrayList<>();

        // Get <pageReference, snippet> pairs from query processor
        Map<PageReference, String> snippets = queryProcessor.getAllPagesSnippets(query);

        // Transform to QueryResult objects
        for (Map.Entry<PageReference, String> entry : snippets.entrySet()) {
            PageReference pageRef = entry.getKey();
            Page page = pageService.getPage(pageRef.getPageId());
            String snippet = entry.getValue();

            results.add(new QueryResult(page.getUrl(), page.getTitle(), snippet));
        }

        return results;
    }
}
