package com.project.searchengine.server.controller;

import com.project.searchengine.server.dto.QueryResult;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.service.*;
import com.project.searchengine.server.service.SearchQueryService;
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

    @GetMapping("/search")
    public ResponseEntity<List<QueryResult>> getQuery(@RequestParam String query) {
        List<QueryResult> results = queryResultService.getQueryResults(query);
        // Save the query to the database
        SearchQuery searchQuery = new SearchQuery(query);
        searchQueryService.saveSearchQuery(searchQuery);

        return ResponseEntity.ok(results);
    }
}
