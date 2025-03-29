package com.project.searchengine.server.service;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    /**
     * Gets total number of documents in the Page database
     *
     * @return number of entries in Page collection
     */
    public long getTotalDocuments(){
        return pageRepository.count();
    }

    public List<Page> getAllPages() {
        return pageRepository.findAll();
    }

    public Page createPage(Page page) {
        return pageRepository.save(page);
    }
}
