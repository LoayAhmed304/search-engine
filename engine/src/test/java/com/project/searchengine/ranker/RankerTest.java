package com.project.searchengine.ranker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RankerTest {

    private Ranker ranker;
    private PageService pageService;
    private Map<String, List<PageReference>> wordResults;
    private String query;

    @BeforeEach
    void setUp() {
        pageService = mock(PageService.class);
        wordResults = new HashMap<>();
        query = "test query";

        // Mock total documents
        when(pageService.getTotalDocuments()).thenReturn(1000L);

        // Create ranker instance with mocked pageService
        ranker = new Ranker(query, wordResults, pageService);
    }

    @Test
    void testRankWithEmptyWordResults() {
        List<String> result = ranker.rank();
        assertTrue(result.isEmpty(), "Rank should return empty list for empty word results");
        System.out.println("Done rank empty word test");
    }

    @Test
    void testProcessTokenWithNoResults() {
        Map<String, Double> scores = new HashMap<>();
        ranker.processToken("nonexistent", scores);
        assertTrue(scores.isEmpty(), "Scores should remain empty for nonexistent token");
        System.out.println("Done test processing tokens with no result");
    }

    @Test
    void testSortedPages() {
        Map<String, Double> scores = new HashMap<>();
        scores.put("page1", 0.9);
        scores.put("page2", 0.7);
        scores.put("page3", 0.8);

        List<String> sorted = ranker.sortedPages(scores);

        assertEquals(3, sorted.size(), "Should contain all pages");
        assertEquals("page1", sorted.get(0), "Highest score should be first");
        assertEquals("page3", sorted.get(1), "Second highest score should be second");
        assertEquals("page2", sorted.get(2), "Lowest score should be last");
        System.out.println("Done testing sorted pages");
    }
}
