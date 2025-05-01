package com.project.searchengine.server.service;

import com.project.searchengine.server.model.SearchQuery;
import com.project.searchengine.server.repository.SearchQueryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryService {

    @Autowired
    private SearchQueryRepository searchQueryRepository;

    public SearchQueryService(SearchQueryRepository searchQueryRepository) {
        this.searchQueryRepository = searchQueryRepository;
    }

    public void saveSearchQuery(SearchQuery query) {
        // Save the search query to the database
        try {
            searchQueryRepository.save(query);
        } catch (Exception e) {
            // Handle the exception if the query already exists

        }
    }

    public List<SearchQuery> suggestions() {
        return searchQueryRepository.findAll();
    }
}
