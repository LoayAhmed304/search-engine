package com.project.searchengine.queryprocessor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.project.searchengine.indexer.StopWordFilter;

import java.util.List;

public class QueryProcessorTokenizationTest {

    private QueryProcessor createProcessor() {
        return new QueryProcessor(new StopWordFilter(), null);
    }

    @Test
    void testEmptyQuery() {
        QueryProcessor processor = createProcessor();
        List<String> result = processor.processQuery("");
        assertTrue(result.isEmpty());
    }

    @Test
    void testRemovesCommonStopWords() {
        QueryProcessor processor = createProcessor();
        List<String> result = processor.processQuery("How to build a gaming PC with the best components");
        assertEquals(List.of("build", "game", "pc", "compon"), result);
    }
}