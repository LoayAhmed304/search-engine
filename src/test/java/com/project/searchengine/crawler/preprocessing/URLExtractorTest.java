package com.project.searchengine.crawler.preprocessing;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

    @Test
    void getURLs_shouldReturnExpectedUrls() {
        List<String> urls = URLExtractor.getURLs(testDocument);

        // Check for some expected URLs (adjust based on actual output)
        assertTrue(urls.contains("https://adventuretime.fandom.com"));
        assertTrue(urls.contains("https://habibayman.github.io/about"));
        assertTrue(urls.contains("https://habibayman.github.io/web-crawler/"));
    }

    @Test
    void getURLs_shouldReturnAbsoluteUrls() {
        List<String> urls = URLExtractor.getURLs(testDocument);

        for (String url : urls) {
            // Check if URL is absolute by looking for protocol:pattern
            assertTrue(url.matches("^[a-zA-Z]+:.*"),
                    "URL should be absolute (contain protocol): " + url);
        }
    }

    @Test
    void filterURLs_shouldExcludeJavaScriptAndMailtoLinks() {
        List<String> testUrls = List.of(
                "https://example.com",
                "javascript:void(0)",
                "mailto:test@example.com",
                "ftp://files.example.com", // Allowed non-HTTP URL
                "https://another-example.com");

        List<String> filtered = URLExtractor.filterURLs(testUrls);

        assertEquals(3, filtered.size(), "Should filter out JavaScript and mailto links");
        assertFalse(filtered.contains("javascript:void(0)"), "JavaScript links should be filtered");
        assertFalse(filtered.contains("mailto:test@example.com"), "Mailto links should be filtered");
        assertTrue(filtered.contains("ftp://files.example.com"), "Non-HTTP URLs should be allowed");
    }

    @Test
    void getURLs_shouldIncludeUrlsWithFragmentsAndParameters() {
        List<String> urls = URLExtractor.getURLs(testDocument);

        // Verify URLs with fragments and parameters are included
        assertTrue(urls.stream().anyMatch(url -> url.contains("#")),
                "Should include URLs with fragments");
        assertTrue(urls.stream().anyMatch(url -> url.contains("?")),
                "Should include URLs with parameters");
    }

    @Test
    void getURLs_shouldReturnUniqueUrls() {
        List<String> urls = URLExtractor.getURLs(testDocument);
        long distinctCount = urls.stream().distinct().count();
        assertEquals(distinctCount, urls.size(), "URLs should be unique");
    }
}