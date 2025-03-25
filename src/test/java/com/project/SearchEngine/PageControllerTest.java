package com.project.SearchEngine;

import com.project.SearchEngine.server.controller.PageController;
import com.project.SearchEngine.server.model.Page;
import com.project.SearchEngine.server.repository.PageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@AutoConfigureMockMvc
public class PageControllerTest {
    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageController pageController;

 
    @Test
    void testGetAllPages() {
        // Arrange
        Page page1 = new Page("1", "https://www.google.com", "Google", "Search engine");
      //  Page page2 = new Page("2", "https://www.bing.com", "Bing", "Search engine");
        List<Page> pages = Arrays.asList(page1);

        when(pageRepository.findAll()).thenReturn(pages);

        // Act
        List<Page> result = pageController.getAllPages();

        // Assert
        assertEquals(1, result.size());
        assertEquals("https://www.google.com", result.get(0).getUrl());
       // assertEquals("https://www.bing.com", result.get(1).getUrl());
    }
}
