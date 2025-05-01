package com.project.searchengine.server.controller;

import com.project.searchengine.server.dto.QueryResult;
import com.project.searchengine.server.service.QueryResultService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class QueryResultController {
    @Autowired 
    private QueryResultService queryResultService;

    @GetMapping("/search")
    public ResponseEntity<List<QueryResult>> getQuery(@RequestParam String query) {
        List<QueryResult> results = queryResultService.getQueryResults(query);
        return ResponseEntity.ok(results);
    }
}