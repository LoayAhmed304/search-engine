package com.project.searchengine.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.project.searchengine.server.controller.PageController;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.service.PageService;
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
    private PageService pageService;

    @InjectMocks
    private PageController pageController;

    @Test
    void testGetAllPages() {
        // Arrange
        Page page1 = new Page("1", "https://www.google.com", "Google", "Search engine");
        List<Page> pages = Arrays.asList(page1);

        // Mock the service method that the controller will call
        when(pageService.getAllPages()).thenReturn(pages);

        // Act
        List<Page> result = pageController.getAllPages();

        // Assert
        assertEquals(1, result.size());
        assertEquals("https://www.google.com", result.get(0).getUrl());
    }
}
