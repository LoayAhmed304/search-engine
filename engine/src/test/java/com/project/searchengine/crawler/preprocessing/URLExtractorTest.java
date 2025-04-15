package com.project.searchengine.crawler.preprocessing;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.util.Set;
import java.util.HashSet;

public class URLExtractorTest {
    private static final String TEST_URL = "https://habibayman.github.io/web-crawler/";
    private static Document testDocument;

    @BeforeAll
    static void setUp() {
        testDocument = URLExtractor.getDocument(TEST_URL);
    }

    @Test
    void getDocument_shouldReturnNonNullDocumentForValidUrl() {
        assertNotNull(testDocument, "Document should not be null for valid URL");
    }

    /**
     * Example specific test case to check if the URLs are extracted correctly.
     */
    @Test
    void getURLs_shouldReturnExpectedUrls() {
        Set<String> urls = URLExtractor.getURLs(testDocument);

        // Check for some expected URLs
        assertTrue(urls.contains("https://adventuretime.fandom.com"));
        assertTrue(urls.contains("https://habibayman.github.io/about"));
        assertTrue(urls.contains("https://habibayman.github.io/web-crawler/"));
    }

    /**
     * Check if the URLs are absolute URLs.
     */
    @Test
    void getURLs_shouldReturnAbsoluteUrls() {
        Set<String> urls = URLExtractor.getURLs(testDocument);

        for (String url : urls) {
            try {
                URI uri = new URI(url);
                assertNotNull(uri.getScheme(), "URL must have a protocol: " + url);

                assertTrue(uri.getHost() != null || uri.getPath() != null,
                        "URL must have either host or path: " + url);

            } catch (URISyntaxException e) {
                fail("Invalid URL: " + url);
            }
        }
    }

    /**
     * Exclude JavaScript and mailto links from the set of URLs.
     */
    @Test
    void filterURLs_shouldExcludeJavaScriptAndMailtoLinks() {
        Set<String> testUrls = new HashSet<>(Set.of(
                "https://example.com",
                "javascript:void(0)",
                "mailto:test@example.com",
                "https://another-example.com"));

        Set<String> filtered = URLExtractor.filterURLs(testUrls);

        assertEquals(2, filtered.size(), "Should filter out JavaScript and mailto links");
        assertFalse(filtered.contains("javascript:void(0)"), "JavaScript links should be filtered");
        assertFalse(filtered.contains("mailto:test@example.com"), "Mailto links should be filtered");
    }
}