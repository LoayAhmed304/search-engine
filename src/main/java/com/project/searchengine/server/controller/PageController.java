package com.project.searchengine.server.controller;

import com.project.searchengine.server.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.searchengine.server.model.Page;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class PageController {

    @Autowired // spring boot creates an object and map it
    private PageService pageService;

    @GetMapping("/pages")
    public List<Page> getAllPages() {
        return pageService.getAllPages();
    }

    @PostMapping("/page")
    public Page createPage(@RequestBody Page page) {
        return pageService.createPage(page);
    }

}
