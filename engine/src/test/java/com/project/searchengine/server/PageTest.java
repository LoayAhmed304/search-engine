package com.project.searchengine.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.project.searchengine.server.model.Page;

public class PageTest {
    
    @Test
    void testCreatePage() {
        // Arrange
        Page page = new Page("1", "https://www.google.com", "Google", "Search engine");
        
        // Act
        String id = page.getId();
        String url = page.getUrl();
        String title = page.getTitle();
        String content = page.getContent();
        
        // Assert
        assertEquals("1", id);
        assertEquals("https://www.google.com", url);
        assertEquals("Google", title);
        assertEquals("Search engine", content);
    }
}
