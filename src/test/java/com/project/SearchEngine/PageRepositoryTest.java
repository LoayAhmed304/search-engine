package com.project.SearchEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.project.SearchEngine.server.model.Page;
import com.project.SearchEngine.server.repository.PageRepository;


@DataMongoTest
public class PageRepositoryTest {

    @Autowired
    private PageRepository pageRepository;

    @Test
    public void testSavePage() {
        // Arrange
        Page page = new Page("1", "https://www.example.com", "example", "Search engine");

        // Act
        pageRepository.save(page);

        // Assert
        Page savedPage = pageRepository.findById("1").get();
        assertEquals("https://www.example.com", savedPage.getUrl());
        assertEquals("example", savedPage.getTitle());
        assertEquals("Search engine", savedPage.getContent());
    }
}
