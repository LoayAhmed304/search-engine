package com.project.SearchEngine.database.repository;

import com.project.SearchEngine.database.model.InvertedIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvertedIndexRepository extends MongoRepository<InvertedIndex, String> {

    InvertedIndex findByWord(String word);

}