package com.project.searchengine.queryprocessor;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;
import com.project.searchengine.server.service.QueryService;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.*;

@Component
public class QueryProcessor {
    private final QueryService queryService;

    private static Integer snippetSize = 30;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private final PorterStemmer stemmer = new PorterStemmer();

    @Autowired
    private QueryTokenizer queryTokenizer;

    @Autowired
    private PhraseMatcher phraseMatcher;

    @Autowired
    private PageRepository pageRepository;

    public QueryProcessor(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Gets the result pages for each token in the processed query.
     *
     * @param tokenizedQuery A list of tokens from the search query.
     * @return A map where the key is the token, and the value is a list of pages
     *         containing that token.
     */
    public Map<String, List<PageReference>> retrieveQueryPages(List<String> tokenizedQuery) {
        Map<String, List<PageReference>> queryPages = new HashMap<>();

        for (String token : tokenizedQuery) {
            List<PageReference> tokenPages = queryService.getTokenPages(token);
            queryPages.put(token, tokenPages);
        }
        return queryPages;
    }

    private String generateSnippet(String[] bodyTokens, int matchPosition, int snippetSize) {
        int halfSnippet = snippetSize / 2;

        int startIndex = Math.max(0, matchPosition - halfSnippet);
        int endIndex = Math.min(bodyTokens.length, matchPosition + halfSnippet);

        StringBuilder snippet = new StringBuilder();

        for (int i = startIndex; i < endIndex; i++) {
            String token = bodyTokens[i];
            String nextToken = (i + 1 < bodyTokens.length) ? bodyTokens[i + 1] : null;

            // starting punctution don't add space after
            if (token.matches("[\\(\\[\\{]")) {
                snippet.append(token);
                // closing punctution don't add space before
            } else if (nextToken != null
                    && nextToken.matches("[.,!?;:’\"'/)\\]\\\\]")) {
                snippet.append(token);
            } else {
                snippet.append(token).append(" ");
            }
        }
        snippet.append("...");

        return snippet.toString().trim();
    }

    /**
     * @param page A list of tokens from the search query.
     * @return body content tokens
     */
    private String[] getPageBodyContent(PageReference referencePage) {
        String pageId = referencePage.getPageId();
        Page page = pageRepository.getPageById(pageId);

        String content = page.getContent();
        Document document = Jsoup.parse(content);
        String bodyContent = document.body().text();

        return tokenizer.tokenize(bodyContent.toLowerCase());
    }

    public Map<PageReference, String> processPages(
            String token,
            List<PageReference> pages,
            QueryTokenizationResult queryTokenizationResult) {

        Map<PageReference, String> pageSnippet = new HashMap<>();
        boolean isPhraseMatch = queryTokenizationResult.getIsPhraseMatch();
        List<String> originalWords = queryTokenizationResult.getOriginalWords();

        for (PageReference page : pages) {
            List<Integer> positions = page.getWordPositions();
            String[] bodyTokens = getPageBodyContent(page);

            // get one snippet only for each page
            for (Integer pos : positions) {
                if (pos >= bodyTokens.length) {
                    continue;
                }

                boolean isMatchFound = false;
                if (isPhraseMatch) {
                    isMatchFound = phraseMatcher.isPhraseMatchFound(bodyTokens, originalWords, token, pos);
                }

                if (!isPhraseMatch || isPhraseMatch && isMatchFound) {
                    String snippet = generateSnippet(bodyTokens, pos, snippetSize);

                    pageSnippet.put(page, snippet);
                    break;
                }
            }
        }
        return pageSnippet;
    }

    /**
     * @param page A list of tokens from the search query.
     * @return body content tokens
     */
    private String getMinPagesToken(Map<String, List<PageReference>> queryPages) {
        String minToken = "";
        int minPagesNumber = Integer.MAX_VALUE;

        for (String token : queryPages.keySet()) {
            List<PageReference> pages = queryPages.get(token);
            if (pages.size() < minPagesNumber) {
                minToken = token;
                minPagesNumber = pages.size();
            }
        }

        return minToken;
    }

    private void displaySnippets(Map<PageReference, String> pageSnippet) {
        for (Map.Entry<PageReference, String> entry : pageSnippet.entrySet()) {
            PageReference page = entry.getKey();
            String snippet = entry.getValue();

            System.out.println("Page: " + page.getPageId());
            System.out.println("-> Snippet: " + snippet);

            System.out.println("--------------------------------------------------");
        }
    }

    public void process(String query) {

        QueryTokenizationResult queryTokenizationResult = queryTokenizer.tokenizeQuery(query);
        List<String> tokenizedQuery = queryTokenizationResult.getTokenizedQuery();

        Map<String, List<PageReference>> queryPages = retrieveQueryPages(tokenizedQuery);

        for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
            String token = entry.getKey();
            List<PageReference> pages = entry.getValue();
            System.out.println("Token: " + token + " | Pages Found: " + (pages != null ? pages.size() : 0));
        }

        // phrase matching
        boolean isPhraseMatch = phraseMatcher.isPhraseMatchQuery(query);

        if (isPhraseMatch) {
            // get the token that has minimum number of result pages
            String minPagesToken = getMinPagesToken(queryPages);
            List<PageReference> minPages = queryPages.get(minPagesToken);

            // get first 20 pages only for now
            if (minPages.size() > 20) {
                minPages = minPages.subList(0, 20);
            }
            // find exact phrase match
            Map<PageReference, String> minPagesSnippets = processPages(minPagesToken, minPages,
                    queryTokenizationResult);
            displaySnippets(minPagesSnippets);

        } else {
            for (String token : queryPages.keySet()) {
                List<PageReference> pages = queryPages.get(token);
                // get first 20 pages only for now
                if (pages.size() > 20) {
                    pages = pages.subList(0, 20);
                }
                Map<PageReference, String> pageSnippets = processPages(token, pages,
                        queryTokenizationResult);
                displaySnippets(pageSnippets);
            }
        }
    }
}
