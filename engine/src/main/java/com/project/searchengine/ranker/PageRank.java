package com.project.searchengine.ranker;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageRank {

    private static final double DAMPING_FACTOR = 0.8;
    private static final short MAX_ITERATIONS = 50;

    private final UrlsFrontierService urlFrontier;
    private final Map<String, UrlDocument> allUrls;
    private final Map<String, Page> allPages;
    private final PageService pageService;

    public PageRank(UrlsFrontierService urlFrontier, PageService pageService) {
        this.urlFrontier = urlFrontier;
        this.pageService = pageService;

        this.allUrls = this.urlFrontier.getAllUrls()
            .stream()
            .collect(Collectors.toMap(UrlDocument::getNormalizedUrl, Function.identity()));
        this.allPages = this.pageService.getAllPages()
            .stream()
            .collect(Collectors.toMap(Page::getUrl, Function.identity()));
    }

    /**
     * Main function to call, which computes all pages ranks
     * @return status boolean (to be implemented later)
     */
    public boolean computeAllRanks() {
        boolean converged = false; // to be implemented later

        for (int i = 0; i < MAX_ITERATIONS && !converged; i++) {
            if (!computePagesRank()) return false;
        }
        // bulk update the pages here

        //⚠️⚠️ implement using bulkOps ⚠️⚠️
        // Tasneem please do it for me thank you
        return true;
    }

    /**
     * Computes the rank for each page in the database (only 1 run)
     * @return status boolean (to be updated later)
     */
    private boolean computePagesRank() {
        Map<String, List<String>> incomingLinks = computeIncomingLinks();

        if (!initializePagesRank()) {
            return false;
        }

        Map<String, Double> newRanks = new HashMap<>();

        for (Page page : allPages.values()) {
            String url = page.getUrl();
            double curRank = 0;

            for (String incoming : incomingLinks.getOrDefault(url, List.of())) {
                Page incomingPage = allPages.get(incoming);
                int outLinks = allUrls
                    .getOrDefault(incoming, new UrlDocument())
                    .getLinkedPages()
                    .size();
                if (outLinks > 0) curRank += incomingPage.getRank() / outLinks;
            }

            curRank *= DAMPING_FACTOR;
            curRank += 1 - DAMPING_FACTOR;
            newRanks.put(url, curRank);
        }

        for (Page page : allPages.values()) {
            page.setRank(newRanks.get(page.getUrl()));
        }

        return true;
    }

    /**
     * Sets all pages rank initially to 1 / N
     * N: Total number of pages in the database
     * @return status boolean
     */
    boolean initializePagesRank() {
        int N = allPages.size();

        for (Page page : allPages.values()) {
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
        for (UrlDocument urlDoc : allUrls.values()) { // loop over every url in the url frontier (not all necessarily crawled)
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
