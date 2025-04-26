package com.project.searchengine.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.repository.PageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
public class PageRepositoryTest {

    @Autowired
    private PageRepository pageRepository;

    @Test
    public void testSavePage() {
        // Arrange
        Page page = new Page("1", "https://www.example.com", "example", "Search engine", 1);

        // Act
        pageRepository.save(page);

        // Assert
        Page savedPage = pageRepository.findById("1").get();
        assertEquals("https://www.example.com", savedPage.getUrl());
        assertEquals("example", savedPage.getTitle());
        assertEquals("Search engine", savedPage.getContent());
    }
}
