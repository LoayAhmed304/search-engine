package com.project.SearchEngine.server.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.SearchEngine.server.model.Page;
import com.project.SearchEngine.server.repository.PageRepository;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class PageController {

    @Autowired  // spring boot creates an object and map it
    private PageRepository pageRepository;

    @GetMapping("/pages")
    public List<Page> getAllPages() {
        return pageRepository.findAll();
    }

    @PostMapping("/page")
    public Page createPage(@RequestBody Page page) {
        return pageRepository.save(page);
    }
    
}
