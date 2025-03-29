package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.InvertedIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvertedIndexRepository extends MongoRepository<InvertedIndex, String> {
    //  InvertedIndex findByWord(String word);

}
