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
    final Map<String, Page> allPages;
    private final PageService pageService;
    private final Map<String, Integer> outgoingLinksCount = new HashMap<>();

    public PageRank(UrlsFrontierService urlFrontier, PageService pageService) {
        this.urlFrontier = urlFrontier;
        this.pageService = pageService;

        this.allUrls = this.urlFrontier.getAllUrls()
            .stream()
            .collect(
                Collectors.toMap(
                    UrlDocument::getNormalizedUrl,
                    Function.identity(),
                    (a, b) -> a,
                    HashMap::new
                )
            );

        computeOutgoingLinksCount();

        this.allPages = this.pageService.getAllPages()
            .stream()
            .collect(Collectors.toMap(Page::getUrl, Function.identity()));
    }

    /**
     * Main function to call, which computes all pages ranks
     * @return status boolean (to be implemented later)
     */
    public boolean computeAllRanks() {
        try {
            boolean converged = false; // to be implemented later

            if (!initializePagesRank()) return false;

            Map<String, List<String>> incomingLinks = computeIncomingLinks();

            for (int i = 0; i < MAX_ITERATIONS && !converged; i++) {
                if (!computePagesRank(incomingLinks)) return false;
            }
            // bulk update the pages here
            // this.pageService.saveAll(new ArrayList<>(allPages.values()));
            this.urlFrontier.saveAll(new ArrayList<>(allUrls.values()));

            return true;
        } finally {
            allUrls.clear();
            allPages.clear();
            outgoingLinksCount.clear();
        }
    }

    /**
     * Computes the rank for each page in the database (only 1 run)
     * @return status boolean (to be updated later)
     */
    boolean computePagesRank(Map<String, List<String>> incomingLinks) {
        // Map<String, Double> newRanks = new HashMap<>(allPages.size());
        Map<String, Double> newRanks = new HashMap<>(allUrls.size());

        // for (Page page : allPages.values()) {
        //     String url = page.getUrl();
        for (UrlDocument doc : allUrls.values()) {
            String url = doc.getNormalizedUrl();
            double curRank = 0;

            for (String incoming : incomingLinks.getOrDefault(url, List.of())) {
                Integer outLinks = outgoingLinksCount.get(incoming);

                if (outLinks != null && outLinks > 0) {
                    // curRank += allPages.get(incoming).getRank() / outLinks;
                    curRank += allUrls.get(incoming).getRank() / outLinks;
                }
            }

            curRank *= DAMPING_FACTOR;
            curRank += 1 - DAMPING_FACTOR;
            newRanks.put(url, curRank);
        }

        // for (Page page : allPages.values()) {
        //     page.setRank(newRanks.get(page.getUrl()));
        // }
        for (UrlDocument doc : allUrls.values()) {
            doc.setRank(newRanks.get(doc.getNormalizedUrl()));
        }

        return true;
    }

    /**
     * Sets all pages rank initially to 1 / N
     * N: Total number of pages in the database
     * @return status boolean
     */
    boolean initializePagesRank() {
        // int N = allPages.size();
        int N = allUrls.size();

        // for (Page page : allPages.values()) {
        //     page.setRank((double) 1 / N);
        // }
        for (UrlDocument doc : allUrls.values()) {
            doc.setRank((double) 1 / N);
        }

        return true;
    }

    /**
     * Computes the incoming links hashmap for each URL in the URL frontier collection
     * @return Adjacency list of all ingoing links for each page
     */
    Map<String, List<String>> computeIncomingLinks() {
        Map<String, List<String>> incomingLinks = new HashMap<>(allUrls.size());

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

    /**
     * Pre computes all the outgoing links sizes
     * Instead of retrieving from the database multiple times for the same URL.
     * To decreases database multiple retrievals to just get a size of an array
     */
    void computeOutgoingLinksCount() {
        for (UrlDocument doc : allUrls.values()) {
            outgoingLinksCount.put(doc.getNormalizedUrl(), doc.getLinkedPages().size());
        }
    }
}
