package com.project.searchengine.indexer;

import java.util.HashSet;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:stopwords.properties")
public class StopWordFilter {

    HashSet<String> stopWords = new HashSet<>();

    public StopWordFilter() {}
}
