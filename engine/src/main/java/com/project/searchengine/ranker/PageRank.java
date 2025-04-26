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
     * @return status boolean (to be implemented later)
     */
    public boolean computeAllRanks() {
        try {
            List<UrlDocument> allUrlsList = urlFrontier.getAllUrls();

            if (allUrlsList == null || allUrlsList.isEmpty()) return false;

            Map<String, UrlDocument> allUrls = allUrlsList
                .stream()
                .collect(Collectors.toMap(UrlDocument::getNormalizedUrl, Function.identity()));

            if (allUrls.isEmpty()) return false;

            computeOutgoingLinksCount(allUrls);

            Map<String, List<String>> incomingLinks = computeIncomingLinks(allUrls);

            currentRanks = initializePagesRank(allUrls);

            Map<String, Double> previousRanks = new HashMap<>(currentRanks);

            int i = 0;
            for (i = 0; i < MAX_ITERATIONS; i++) {
                System.out.println("Pagerank computation loop #" + i + ". Not converged yet");
                currentRanks = computePagesRank(incomingLinks, currentRanks);

                if (hasConverged(previousRanks, currentRanks)) break;
                previousRanks = new HashMap<>(currentRanks);
            }
            System.out.println("Finished the loop - converged after " + (i + 1) + " iterations");

            // bulk update the pages here, the currentRanks
            pageService.setRanks(currentRanks);
            return true;
        } catch (Exception e) {
            System.out.println("Exception in PageRank: " + e);
            return false;
        } finally {
            outgoingLinksCount.clear();
        }
    }

    private boolean hasConverged(Map<String, Double> oldRanks, Map<String, Double> newRanks) {
        double totalChange = 0.0;
        for (Map.Entry<String, Double> entry : newRanks.entrySet()) {
            String url = entry.getKey();
            double newRank = entry.getValue();
            double oldRank = oldRanks.getOrDefault(url, 0.0);
            totalChange += Math.abs(newRank - oldRank);
        }

        double averageChange = totalChange / newRanks.size();
        return averageChange < CONVERGE_THRESHOLD;
    }

    /**
     * Computes the rank for each page in the database (only 1 run)
     * @return status boolean (to be updated later)
     */
    Map<String, Double> computePagesRank(
        Map<String, List<String>> incomingLinks,
        Map<String, Double> currentRanks
    ) {
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
     * Sets all pages rank initially to 1 / N
     * N: Total number of pages in the database
     * @return status boolean
     */
    Map<String, Double> initializePagesRank(Map<String, UrlDocument> allUrls) {
        double initialRank = 1.0 / allUrls.size();

        return allUrls
            .keySet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), url -> initialRank));
    }

    /**
     * Computes the incoming links hashmap for each URL in the URL frontier collection
     * @return Adjacency list of all ingoing links for each page
     */
    Map<String, List<String>> computeIncomingLinks(Map<String, UrlDocument> allUrls) {
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
    void computeOutgoingLinksCount(Map<String, UrlDocument> allUrls) {
        for (UrlDocument doc : allUrls.values()) {
            outgoingLinksCount.put(doc.getNormalizedUrl(), doc.getLinkedPages().size());
        }
    }
}
