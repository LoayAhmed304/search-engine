package com.project.searchengine.indexer;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:stopwords.properties")
@Component
public class StopWordFilter {

    Set<String> stopWords;

    public StopWordFilter(@Value("${stop-words}") String stopWords) {
        this.stopWords = new HashSet<>(Arrays.asList(stopWords.split(",")));
        System.out.println("Stop words loaded: " + this.stopWords);
    }

    public boolean isStopWord(String token) {
        return stopWords.contains(token);
    }
}
