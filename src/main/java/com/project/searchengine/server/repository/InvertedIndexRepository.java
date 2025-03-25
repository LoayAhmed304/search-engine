package com.project.searchengine.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.project.searchengine.server.model.InvertedIndex;

@Repository
public interface InvertedIndexRepository extends MongoRepository<InvertedIndex, String> {

   //  InvertedIndex findByWord(String word);

}