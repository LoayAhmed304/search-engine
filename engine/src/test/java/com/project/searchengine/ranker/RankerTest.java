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
    void testRankWithSingleToken() {
        // Setup mock page references
        PageReference pr1 = mock(PageReference.class);
        when(pr1.getPageId()).thenReturn("page1");
        when(pr1.getPageRank()).thenReturn(0.8);
        // Mock methods for TF calculation
        when(pr1.getWordPositions()).thenReturn(Arrays.asList(1, 2)); // Size = 2
        when(pr1.getPageTokenCount()).thenReturn(1); // TF = 2 / 1 = 2.0

        PageReference pr2 = mock(PageReference.class);
        when(pr2.getPageId()).thenReturn("page2");
        when(pr2.getPageRank()).thenReturn(0.6);
        // Mock methods for TF calculation
        when(pr2.getWordPositions()).thenReturn(Collections.singletonList(1)); // Size = 1
        when(pr2.getPageTokenCount()).thenReturn(1); // TF = 1 / 1 = 1.0

        wordResults.put("test", Arrays.asList(pr1, pr2));

        List<String> result = ranker.rank();

        assertEquals(2, result.size(), "Should return two pages");
        assertEquals("page1", result.get(0), "Page1 should be first due to higher score");
        assertEquals("page2", result.get(1), "Page2 should be second");
        System.out.println("Done test with single token");
    }

    @Test
    void testComputeScoresWithMultipleTokens() {
        PageReference pr1 = mock(PageReference.class);
        when(pr1.getPageId()).thenReturn("page1");
        when(pr1.getPageRank()).thenReturn(0.8);
        when(pr1.getWordPositions()).thenReturn(Arrays.asList(1, 2)); // Size = 2
        when(pr1.getPageTokenCount()).thenReturn(1); // TF = 2 / 1 = 2.0

        wordResults.put("test", Collections.singletonList(pr1));
        wordResults.put("query", Collections.singletonList(pr1));

        Map<String, Double> scores = ranker.computeScores();

        assertEquals(1, scores.size(), "Should have one page in scores");
        assertTrue(scores.containsKey("page1"), "Scores should contain page1");
        assertTrue(scores.get("page1") > 0, "Page1 score should be positive");
        System.out.println("Done test with multiple tokens");
    }

    @Test
    void testGetIDF() {
        double idf = ranker.getIDF(100);
        double expected = Math.log(1000.0 / 100);
        assertEquals(expected, idf, 0.0001, "IDF calculation should be correct");
        System.out.println("Done testing IDF");
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
