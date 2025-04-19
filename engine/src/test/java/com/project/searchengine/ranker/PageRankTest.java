package com.project.searchengine.ranker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class PageRankTest {

    private UrlsFrontierService urlFrontier;
    private PageService pageService;
    private PageRank pageRank;

    @BeforeEach
    void setUp() {
        urlFrontier = mock(UrlsFrontierService.class);
        pageService = mock(PageService.class);
        pageRank = new PageRank(urlFrontier, pageService);
    }

    @Test
    void testInitializePagesRank() throws Exception {
        // Setup test data
        Map<String, Page> testPages = new HashMap<>();
        testPages.put("url1", new Page("1", "url1", "xTitle", "xContent"));
        testPages.put("url2", new Page("2", "url2", "yTitle", "yContent"));
        testPages.put("url3", new Page("3", "url3", "zTitle", "zContent"));

        // Mock the page service to return our test pages
        when(pageService.getAllPages()).thenReturn(new ArrayList<>(testPages.values()));

        // Create a new PageRank instance
        PageRank ranker = new PageRank(urlFrontier, pageService);

        // Use reflection to access private field for testing
        try {
            java.lang.reflect.Field allPagesField = PageRank.class.getDeclaredField("allPages");
            allPagesField.setAccessible(true);
            allPagesField.set(ranker, testPages);
        } catch (Exception e) {
            fail("Failed to set test data via reflection: " + e.getMessage());
        }

        // Execute
        boolean result = ranker.initializePagesRank();

        // Verify
        assertTrue(result);
        assertEquals(1.0 / 3, testPages.get("url1").getRank());
        assertEquals(1.0 / 3, testPages.get("url2").getRank());
        assertEquals(1.0 / 3, testPages.get("url3").getRank());
    }

    @Test
    void testComputeIncomingLinks() throws Exception {
        // Setup test URLs with links

        /*
         * (String normalizedUrl, long frequency, boolean isCrawled,
         * String document, String hashedDocContent,
         * List<String> linkedPages, String lastCrawled)
         */
        //
        UrlDocument url1 = new UrlDocument(
            "url1",
            1,
            true,
            "d",
            "hd",
            Arrays.asList("url2", "url3"),
            "now"
        );
        UrlDocument url2 = new UrlDocument(
            "url2",
            1,
            true,
            "d",
            "hd",
            Arrays.asList("url3"),
            "now"
        );
        UrlDocument url3 = new UrlDocument(
            "url3",
            1,
            true,
            "d",
            "hd",
            Collections.emptyList(),
            "now"
        );

        // Mock URL frontier to return our test URLs
        when(urlFrontier.getAllUrls()).thenReturn(Arrays.asList(url1, url2, url3));

        // Create PageRank instance and set private allUrls field
        PageRank ranker = new PageRank(urlFrontier, pageService);
        java.lang.reflect.Field allUrlsField = PageRank.class.getDeclaredField("allUrls");
        allUrlsField.setAccessible(true);
        allUrlsField.set(ranker, Map.of("url1", url1, "url2", url2, "url3", url3));

        // Execute
        Map<String, List<String>> incomingLinks = ranker.computeIncomingLinks();

        // Verify
        assertEquals(0, incomingLinks.getOrDefault("url1", List.of()).size());
        assertEquals(1, incomingLinks.get("url2").size());
        assertTrue(incomingLinks.get("url2").contains("url1"));
        assertEquals(2, incomingLinks.get("url3").size());
        assertTrue(incomingLinks.get("url3").containsAll(Arrays.asList("url1", "url2")));
    }

    @Test
    void testComputePagesRank() throws Exception {
        // Setup test data
        Map<String, Page> testPages = new HashMap<>();
        testPages.put("url1", new Page("1", "url1", "xTitle", "xContent"));
        testPages.put("url2", new Page("2", "url2", "yTitle", "yContent"));
        testPages.put("url3", new Page("3", "url3", "zTitle", "zContent"));

        for (Page page : testPages.values()) {
            page.setRank(1.0 / testPages.size());
        }

        Map<String, UrlDocument> testUrls = new HashMap<>();
        testUrls.put(
            "url1",
            new UrlDocument("url1", 1, true, "d", "hd", Arrays.asList("url2", "url3"), "now")
        );
        testUrls.put(
            "url2",
            new UrlDocument("url2", 1, true, "d", "hd", Arrays.asList("url3"), "now")
        );
        testUrls.put(
            "url3",
            new UrlDocument("url3", 1, true, "d", "hd", Collections.emptyList(), "now")
        );

        Map<String, List<String>> incomingLinks = Map.of(
            "url1",
            Collections.emptyList(),
            "url2",
            Arrays.asList("url1"),
            "url3",
            Arrays.asList("url1", "url2")
        );

        // Use reflection to set private fields
        PageRank ranker = new PageRank(urlFrontier, pageService);
        java.lang.reflect.Field allPagesField = PageRank.class.getDeclaredField("allPages");
        allPagesField.setAccessible(true);
        allPagesField.set(ranker, testPages);

        java.lang.reflect.Field allUrlsField = PageRank.class.getDeclaredField("allUrls");
        allUrlsField.setAccessible(true);
        allUrlsField.set(ranker, testUrls);

        java.lang.reflect.Field outgoingCountsField =
            PageRank.class.getDeclaredField("outgoingLinksCount");
        outgoingCountsField.setAccessible(true);
        outgoingCountsField.set(ranker, Map.of("url1", 2, "url2", 1, "url3", 0));

        // Execute
        boolean result = ranker.computePagesRank(incomingLinks);

        // Verify
        assertTrue(result);
        assertEquals(0.2, testPages.get("url1").getRank(), 0.001); // (1-d)/N
        assertEquals((0.333 * 0.8) / 2 + 0.2, testPages.get("url2").getRank(), 0.001);
        assertEquals(
            ((0.333 * 0.8) / 2) + ((0.333 * 0.8) / 1) + 0.2,
            testPages.get("url3").getRank(),
            0.001
        );
    }

    @Test
    void testComputeAllRanks() {
        // 1. Create test Pages FIRST (using same constructor as production)
        Page page1 = new Page("url1", "url1", "xTitle", "xContent");
        Page page2 = new Page("url2", "url2", "yTitle", "yContent");
        Page page3 = new Page("url3", "url3", "zTitle", "zContent");

        // 2. Create test UrlDocuments
        UrlDocument url1 = new UrlDocument(
            "url1", // Make sure this matches page1's URL exactly
            1,
            true,
            "d",
            "hd",
            Arrays.asList("url2", "url3"), // url1 links to url2 and url3
            "now"
        );
        UrlDocument url2 = new UrlDocument(
            "url2",
            1,
            true,
            "d",
            "hd",
            Arrays.asList("url3"), // url2 links to url3
            "now"
        );
        UrlDocument url3 = new UrlDocument(
            "url3",
            1,
            true,
            "d",
            "hd",
            Collections.emptyList(), // url3 has no outgoing links
            "now"
        );

        // 3. Configure mocks to return OUR instances
        when(pageService.getAllPages()).thenReturn(Arrays.asList(page1, page2, page3));
        when(urlFrontier.getAllUrls()).thenReturn(Arrays.asList(url1, url2, url3));

        // 4. Capture what was passed to saveAll(), because we clear the data structures now
        ArgumentCaptor<List<Page>> pagesCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(pageService).saveAll(pagesCaptor.capture());

        // 5. Execute
        PageRank ranker = new PageRank(urlFrontier, pageService);
        boolean result = ranker.computeAllRanks();
        assertTrue(result);

        // 6. Verify saved pages
        List<Page> savedPages = pagesCaptor.getValue();
        assertNotNull(savedPages);
        assertEquals(3, savedPages.size());
        verify(pageService, times(1)).saveAll(anyList());

        // 7. Verify we're testing the right instances
        assertEquals(page1, ranker.allPages.get("url1"));
        assertEquals(page2, ranker.allPages.get("url2"));
        assertEquals(page3, ranker.allPages.get("url3"));

        // 8. Verify ranks updated properly
        assertTrue(page1.getRank() > 0, "Page1 rank should be > 0");
        assertTrue(page2.getRank() > 0, "Page2 rank should be > 0");
        assertTrue(page3.getRank() > 0, "Page3 rank should be > 0");

        // 9. Verify rank distribution makes sense
        assertTrue(
            page3.getRank() > page2.getRank(),
            "url3 should have higher rank than url2 (more incoming links)"
        );
        assertTrue(
            page2.getRank() > page1.getRank(),
            "url2 should have higher rank than url1 (more incoming links)"
        );
    }
}
