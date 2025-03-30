package com.project.searchengine.crawler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RobotsHandlerTest {

    @Test
    public void testAllowedUrl_ReturnsTrue() {
        RobotsHandler handler = new RobotsHandler();
        // Google's robots.txt generally allows most paths
        boolean result = handler.isUrlAllowed("https://www.google.com/search/about");
        assertTrue(result, "Google's /search/about should be allowed");
    }

    @Test
    public void testDisallowedUrl_ReturnsFalse() {
        RobotsHandler handler = new RobotsHandler();
        // Facebook's robots.txt disallows most scraping
        boolean result = handler.isUrlAllowed("https://www.facebook.com/private");
        assertFalse(result, "Facebook's private paths should be disallowed");
    }
}