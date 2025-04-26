package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.Page;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PageRepository extends MongoRepository<Page, String> {
    /**
     * Check if a page already exists in the database
     *
     */
    public boolean existsById(String id);

    public Page getPageById(String id);

    @Query(value = "{ '_id': {$in:?0}}", fields = "{ 'rank' :1 }")
    List<Page> findRanksByIds(List<String> ids);
}
