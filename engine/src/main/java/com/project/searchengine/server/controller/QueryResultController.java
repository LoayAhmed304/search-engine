package com.project.searchengine.server.controller;

import com.project.searchengine.server.dto.QueryResult;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.service.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class QueryResultController {

    @Autowired
    private QueryResultService queryResultService;

    @Autowired
    private SearchQueryService searchQueryService;

    /**
     * Handles GET requests to the /search endpoint.
     * Processes the search query and returns a list of QueryResult objects.
     * 
     * @param query The search query to process.
     * @return A ResponseEntity containing a list of QueryResult objects.
     */
    @GetMapping("/search")
    public ResponseEntity<List<QueryResult>> getQuery(@RequestParam String query, @RequestParam(defaultValue = "0") int pageNumber) {
        List<QueryResult> results = queryResultService.getQueryResults(query, pageNumber);
        
        // Save the query to the database
        SearchQuery searchQuery = new SearchQuery(query);
        searchQueryService.saveSearchQuery(searchQuery);

        return ResponseEntity.ok(results);
    }

    /**
     * Handles GET requests to the /suggestions endpoint.s
     * Returns a list of all search queries in the database.
     * 
     * @return A ResponseEntity containing a list of SearchQuery objects.
     */
    @GetMapping("/history")
    public ResponseEntity<List<SearchQuery>> getSuggestions() {
        List<SearchQuery> suggestions = searchQueryService.suggestions();
        return ResponseEntity.ok(suggestions);
    }
}
