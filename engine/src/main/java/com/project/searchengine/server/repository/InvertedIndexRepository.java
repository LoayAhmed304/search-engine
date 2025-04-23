package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.InvertedIndex;
import java.util.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvertedIndexRepository extends MongoRepository<InvertedIndex, String> {
    List<InvertedIndex> findAllByWordIn(Collection<String> words);
}
