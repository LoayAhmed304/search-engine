package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.UrlDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlsFrontierRepository extends MongoRepository<InvertedIndex, String> {
    
    List<UrlDocument> findTop100ByIsCrawledFalseOrderByFrequencyDesc();

    @Update("{ '$inc': { 'frequency': 1 } }")
    void incrementFrequency(@Param("normalizedUrl") String normalizedUrl);

    boolean existsByNormalizedUrl(String normalizedUrl);
}
