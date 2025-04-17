package com.project.searchengine.ranker;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.repository.UrlsFrontierRepository;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import java.util.*;

public class PageRank {

    private final UrlsFrontierService urlFrontier;
    private final List<UrlDocument> allUrls;
    private final List<Page> allPages;
    private final PageService pageService;

    public PageRank(UrlsFrontierService urlFrontier, PageService pageService) {
        this.urlFrontier = urlFrontier;
        this.pageService = pageService;

        this.allUrls = this.urlFrontier.getAllUrls();
        this.allPages = pageService.getAllPages();
    }

    public boolean computePagesRank() {
        Map<String, List<String>> incomingLinks = computeIncomingLinks();
        initializePagesRank();
        return true;
    }

    /**
     * Sets all pages rank initially to 1 / N
     * N: Total number of pages in the database
     */
    boolean initializePagesRank() {
        int N = allPages.size();

        for (Page page : allPages) {
            page.setRank((double) 1 / N);
        }

        return true;
    }

    /**
     * Computes the incoming links hashmap for each URL in the URL frontier collection
     * @return Adjacency list of all ingoing links for each page
     */
    private Map<String, List<String>> computeIncomingLinks() {
        Map<String, List<String>> incomingLinks = new HashMap<>();

        // for every page, add it to the outgoing links from the url frontier
        for (UrlDocument urlDoc : allUrls) { // loop over every url in the url frontier (not all necessarily crawled)
            String curUrl = urlDoc.getNormalizedUrl(); // extract its url
            List<String> outgoingPagesUrls = urlDoc.getLinkedPages(); // extract its outgoing links array

            // loop over every link in the outgoing links array, and add current url to its incoming urls map
            for (String pageLink : outgoingPagesUrls) {
                incomingLinks.computeIfAbsent(pageLink, k -> new ArrayList<>()).add(curUrl);
            }
        }

        return incomingLinks;
    }
}
