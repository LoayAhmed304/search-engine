package com.project.searchengine.server.controller;

import com.project.searchengine.server.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class QueryController {
    @Autowired 
    private QueryService queryService;

    @GetMapping("/query")
    public String getQuery(@RequestParam String query) {
        System.out.println(queryService.getQueryResults(query).toString());
        return queryService.getQueryResults(query).toString();
    }
    
}