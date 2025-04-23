package com.project.searchengine.processor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.project.searchengine.indexer.StopWordFilter;

import java.util.List;

public class ProcessorTokenizationTest {

    private Processor createProcessor() {
        return new Processor(new StopWordFilter(), null);
    }

    @Test
    void testEmptyQuery() {
        Processor processor = createProcessor();
        List<String> result = processor.processQuery("");
        assertTrue(result.isEmpty());
    }

    @Test
    void testRemovesCommonStopWords() {
        Processor processor = createProcessor();
        List<String> result = processor.processQuery("How to build a gaming PC with the best components");
        assertEquals(List.of("build", "game", "pc", "compon"), result);
    }
}