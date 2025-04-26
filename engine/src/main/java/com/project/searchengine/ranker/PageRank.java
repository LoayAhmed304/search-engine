package com.project.searchengine.ranker;

import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PageRank {

    private static final double DAMPING_FACTOR = 0.85;
    private static final short MAX_ITERATIONS = 100;
    private static final double CONVERGE_THRESHOLD = 1e-7;

    private final UrlsFrontierService urlFrontier;
    private final PageService pageService;
    private final Map<String, Integer> outgoingLinksCount = new HashMap<>();
    private Map<String, Double> currentRanks;

    public PageRank(UrlsFrontierService urlFrontier, PageService pageService) {
        this.urlFrontier = urlFrontier;
        this.pageService = pageService;
    }

    /**
     * Main function to call, which computes all pages ranks
     * @return status boolean
     */
    public boolean computeAllRanks() {
        try {
            // 1) Compute the map to be easier to retrieve data:  <String url, UrlDocument>
            List<UrlDocument> allUrlsList = urlFrontier.getAllUrls();
            if (allUrlsList == null || allUrlsList.isEmpty()) return false;

            Map<String, UrlDocument> allUrls = mapUrls(allUrlsList);
            if (allUrls.isEmpty()) return false;

            // 2) Initialize the outgoing links count, incoming links map, and rank for all pages
            computeOutgoingLinksCount(allUrls);
            Map<String, List<String>> incomingLinks = computeIncomingLinks(allUrls);
            currentRanks = initializePagesRank(allUrls);

            // 3) Main loop
            runPageRankIterations(incomingLinks);

            // 4) Bulk update all pages ranks in the database
            pageService.setRanks(currentRanks);
            return true;
        } catch (Exception e) {
            System.out.println("Exception in PageRank: " + e);
            return false;
        } finally {
            outgoingLinksCount.clear();
        }
    }

    /**
     * Keep iterating over all pages and update their page rank at each iteration till convergence or threshold
     * @param incomingLinks Adjancency list that has the incoming urls for each url
     */
    private void runPageRankIterations(Map<String, List<String>> incomingLinks) {
        Map<String, Double> previousRanks = new HashMap<>(currentRanks); // For convergence checking

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            System.out.println("Pagerank computation loop #" + i + ". Not converged yet");
            currentRanks = computePagesRank(incomingLinks);

            if (hasConverged(previousRanks)) {
                System.out.println("Converged after " + (i + 1) + " iterations");
                break;
            }
            previousRanks = new HashMap<>(currentRanks);
        }
    }

    /**
     * Computes all pages ranks from one another (one iteration)
     * @param incomingLinks List containing the documents that point to each document
     * @return New ranks after this computation iteration
     */
    Map<String, Double> computePagesRank(Map<String, List<String>> incomingLinks) {
        Map<String, Double> newRanks = new ConcurrentHashMap<>();

        currentRanks
            .keySet()
            .parallelStream()
            .forEach(url -> {
                double curRank = 0;

                for (String incoming : incomingLinks.getOrDefault(url, List.of())) {
                    Integer outLinksCount = outgoingLinksCount.get(incoming);

                    if (outLinksCount != null && outLinksCount > 0) {
                        Double incomingRank = currentRanks.get(incoming);
                        if (incomingRank == null) continue;
                        curRank += currentRanks.get(incoming) / outLinksCount;
                    }
                }

                curRank *= DAMPING_FACTOR;
                curRank += 1 - DAMPING_FACTOR;
                newRanks.put(url, curRank);
            });

        return newRanks;
    }

    /**
     * Sets all pages ranks initially to 1 / N
     * N: total number of documents in the database
     *
     * @param allUrls Map <String url, UrlDocument> containing all urlsFrontier collection
     * @return Map<String url, Double rank> containing the current (initial) pages rank
     */
    Map<String, Double> initializePagesRank(Map<String, UrlDocument> allUrls) {
        double initialRank = 1.0 / allUrls.size();

        return allUrls
            .keySet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), url -> initialRank));
    }

    /**
     * Converts the outgoing links computed by the crawler in the urlsFrontier collection, into incoming links for each page
     *
     * @param allUrls Map<String url, UrlDocument> containing the urlFrontier collection, with url as its key
     * @return adjacency list contianing all incoming links for all pages in the database
     */
    Map<String, List<String>> computeIncomingLinks(Map<String, UrlDocument> allUrls) {
        Map<String, List<String>> incomingLinks = new HashMap<>(allUrls.size());

        // for every page, add it to the outgoing links from the url frontier
        for (UrlDocument urlDoc : allUrls.values()) { // loop over every url in the url frontier (not all necessarily crawled)
            String curUrl = urlDoc.getNormalizedUrl(); // extract its url

            // loop over every link in the outgoing links array, and add current url to its incoming urls map
            for (String pageLink : urlDoc.getLinkedPages()) {
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
    void computeOutgoingLinksCount(Map<String, UrlDocument> allUrls) {
        for (UrlDocument doc : allUrls.values()) {
            outgoingLinksCount.put(doc.getNormalizedUrl(), doc.getLinkedPages().size());
        }
    }

    /**
     * Check if the pages ranks have converged.
     * @param oldRanks Previous iterations pages ranks
     * @param newRanks Current pages ranks
     * @return boolean indicating whether it has converged
     */
    private boolean hasConverged(Map<String, Double> oldRanks) {
        double totalChange = 0.0;
        for (Map.Entry<String, Double> entry : currentRanks.entrySet()) {
            String url = entry.getKey();
            double newRank = entry.getValue();
            double oldRank = oldRanks.getOrDefault(url, 0.0);
            totalChange += Math.abs(newRank - oldRank);
        }

        double averageChange = totalChange / currentRanks.size();
        return averageChange < CONVERGE_THRESHOLD;
    }

    private Map<String, UrlDocument> mapUrls(List<UrlDocument> allUrlsList) {
        return allUrlsList
            .stream()
            .collect(Collectors.toMap(UrlDocument::getNormalizedUrl, Function.identity()));
    }
}
