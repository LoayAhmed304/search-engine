package com.project.searchengine.queryprocessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.searchengine.indexer.StopWordFilter;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.QueryService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryProcessorTest {

    private QueryProcessor queryProcessor;
    private QueryService queryService;
    private StopWordFilter stopWordFilter;
    private String query;

    @BeforeEach
    void setUp() {
        queryService = mock(QueryService.class);
        stopWordFilter = new StopWordFilter(); // Using real implementation
        queryProcessor = new QueryProcessor(stopWordFilter, queryService);
        query = "test query";
    }

    @Test
    void testProcessQueryWithEmptyString() {
        List<String> result = queryProcessor.processQuery("");
        assertTrue(result.isEmpty(), "Should return empty list for empty query");
        System.out.println("Done empty query test");
    }

    @Test
    void testProcessQueryWithStopWords() {
        List<String> result = queryProcessor.processQuery("the and or but");
        assertTrue(result.isEmpty(), "Should return empty list for all stop words");
        System.out.println("Done stop words test");
    }

    @Test
    void testProcessQueryWithNormalText() {
        List<String> result = queryProcessor.processQuery("Searching for documents");
        assertEquals(Arrays.asList("search", "document"), result,
                "Should stem and lowercase properly");
        System.out.println("Done normal text processing test");
    }

    @Test
    void testRetrieveQueryPagesWithNoResults() {
        Map<String, List<PageReference>> result = queryProcessor
                .retrieveQueryPages(Collections.singletonList("nonexistent"));

        assertTrue(result.containsKey("nonexistent"), "Should contain key for token");
        assertTrue(result.get("nonexistent").isEmpty(), "Should return empty list for no results");
        System.out.println("Done no results test");
    }

    @Test
    void testRetrieveQueryPagesWithSingleToken() {
        // Setup mock page references
        PageReference pr1 = mock(PageReference.class);
        when(pr1.getPageId()).thenReturn("page1");
        when(pr1.getPageRank()).thenReturn(0.8);

        PageReference pr2 = mock(PageReference.class);
        when(pr2.getPageId()).thenReturn("page2");
        when(pr2.getPageRank()).thenReturn(0.6);

        when(queryService.getTokenPages("test"))
                .thenReturn(Arrays.asList(pr1, pr2));

        Map<String, List<PageReference>> result = queryProcessor.retrieveQueryPages(Collections.singletonList("test"));

        assertEquals(1, result.size(), "Should have one token entry");
        assertEquals(2, result.get("test").size(), "Should return two pages");
        assertEquals("page1", result.get("test").get(0).getPageId());
        assertEquals("page2", result.get("test").get(1).getPageId());
        System.out.println("Done single token retrieval test");
    }

    @Test
    void testRetrieveQueryPagesWithMultipleTokens() {
        // Setup mock page references
        PageReference pr1 = mock(PageReference.class);
        when(pr1.getPageId()).thenReturn("page1");

        PageReference pr2 = mock(PageReference.class);
        when(pr2.getPageId()).thenReturn("page2");

        when(queryService.getTokenPages("search"))
                .thenReturn(Collections.singletonList(pr1));
        when(queryService.getTokenPages("engine"))
                .thenReturn(Collections.singletonList(pr2));

        Map<String, List<PageReference>> result = queryProcessor.retrieveQueryPages(Arrays.asList("search", "engine"));

        assertEquals(2, result.size(), "Should have entries for both tokens");
        assertEquals(1, result.get("search").size(), "Should return one page for 'search'");
        assertEquals(1, result.get("engine").size(), "Should return one page for 'engine'");
        System.out.println("Done multiple tokens retrieval test");
    }

    @Test
    void testFullProcessingFlow() {
        // Setup mock page references
        PageReference pr1 = mock(PageReference.class);
        when(pr1.getPageId()).thenReturn("math-guide");

        PageReference pr2 = mock(PageReference.class);
        when(pr2.getPageId()).thenReturn("math-tips");

        when(queryService.getTokenPages("studi"))
                .thenReturn(Arrays.asList(pr1, pr2));
        when(queryService.getTokenPages("math"))
                .thenReturn(Collections.singletonList(pr1));

        // Process query and retrieve pages
        List<String> tokens = queryProcessor.processQuery("how to study math");
        Map<String, List<PageReference>> results = queryProcessor.retrieveQueryPages(tokens);

        // Verify processing
        assertEquals(Arrays.asList("studi", "math"), tokens);

        // Verify results
        assertEquals(2, results.size(), "Should have entries for both tokens");
        assertEquals(2, results.get("studi").size(), "Should return two pages for 'studi'");
        assertEquals(1, results.get("math").size(), "Should return one page for 'math'");
        System.out.println("Done full processing flow test");
    }
}