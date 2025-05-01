package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.SearchQuery;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchQueryRepository extends MongoRepository<SearchQuery, String> {
    List<SearchQuery> findAll();
}
