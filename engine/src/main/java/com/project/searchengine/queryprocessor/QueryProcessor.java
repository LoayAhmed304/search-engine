package com.project.searchengine.queryprocessor;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.indexer.StopWordFilter;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;
import com.project.searchengine.server.service.QueryService;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.*;

@Component
public class QueryProcessor {
    private final QueryService queryService;
    
    private boolean isPhraseMatcher;
    private static Integer snippetSize = 30;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

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

    /**
     * @param query
     * @return whether it is a phrase matching query or not
     */
    private boolean isPhraseMatchQuery(String query) {
        if (query.isEmpty() || query.length() < 2) {
            return false;
        }

        return (query.charAt(0) == '\"'
                && query.charAt(query.length() - 1) == '\"')
                        ? true
                        : false;
    }

    public String generateSnippet(String[] bodyTokens, int matchPosition, int snippetSize) {
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

        return snippet.toString().trim();
    }

    /**
     * @param page A list of tokens from the search query.
     * @return body content tokens
     */
    private String[] getPageBodyContent(PageReference page) {
        String pageId = page.getPageId();
        Optional<Page> pageOptional = pageRepository.findById(pageId);
        Page fullPage = pageOptional.get();

        String content = fullPage.getContent();
        Document document = Jsoup.parse(content);
        String bodyContent = document.body().text();

        return tokenizer.tokenize(bodyContent.toLowerCase());
    }


    private boolean isPhraseMatchFound(String[] bodyTokens, String token, int pos) {
        // check positions around it
        int tokenIndex = this.originalWords.indexOf(token);
        int querySize = originalWords.size();

        // System.out.println(tokenIndex + " " + querySize);

        boolean found = true;

        // check letters before
        int offset = pos - 1;
        int prevTokenIndex = tokenIndex - 1;

        while (found && prevTokenIndex >= 0 && offset >= 0) {
            String prevToken = originalWords.get(prevTokenIndex);

            if (!prevToken.equals(bodyTokens[offset])) {
                found = false;
            }
            offset--;
            prevTokenIndex--;
        }

        offset = pos + 1;
        int nextTokenIndex = tokenIndex + 1;

        while (found && offset < bodyTokens.length && nextTokenIndex < querySize) {
            String nextToken = originalWords.get(nextTokenIndex);

            if (!nextToken.equals(bodyTokens[offset])) {
                found = false;
            }
            offset++;
            nextTokenIndex++;
        }
        return found;
    }
    
    /**
     * generate snippets from result pages
     * 
     * @param token
     * @param pages reference pages for each token
     * @return map of each page and its snippets
     */
    public Map<PageReference, List<String>> processPages(String token, List<PageReference> pages) {
        Map<PageReference, List<String>> pageSnippets = new HashMap<>();
        String originalWord = processedWordToOriginal.get(token);

        for (PageReference page : pages) {
            List<Integer> positions = page.getWordPositions();
            String[] bodyTokens = getPageBodyContent(page);
            List<String> snippets = new ArrayList<>();

            for (Integer pos : positions) {
                if (pos >= bodyTokens.length) {
                    continue;
                }

                // exclude header positions for now 
                String stemmedToken = stemmer.stem(bodyTokens[pos]);
                if (!stemmedToken.equals(token)) {
                    continue;
                }

                if (isPhraseMatcher && bodyTokens[pos].equals(originalWord)) {
                    boolean found = isPhraseMatchFound(bodyTokens, token, pos);
                    if (found) {
                        String snippet = generateSnippet(bodyTokens, pos, snippetSize);
                        snippets.add(snippet);
                    }
                }

                if (!isPhraseMatcher) {
                    String snippet = generateSnippet(bodyTokens, pos, snippetSize);
                    snippets.add(snippet);
                }
            }

            if (!snippets.isEmpty()) {
                pageSnippets.put(page, snippets);
            }
        }
        return pageSnippets;
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

    public void process(String query) {
        this.isPhraseMatcher = isPhraseMatchQuery(query);

        List<String> processedQuery = processQuery(query);

        Map<String, List<PageReference>> queryPages = retrieveQueryPages(processedQuery);

        String minToken = getMinPagesToken(queryPages);
        List<PageReference> minTokenPages = queryPages.get(minToken);

        System.out.println("min token: " + minToken);
        int minTokenIndex = this.originalWords.indexOf(minToken);

        System.out.println(minTokenIndex);

        // for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
        // String token = entry.getKey();
        // List<PageReference> pages = entry.getValue();
        // System.out.println("Token: " + token + " | Pages Found: " + (pages != null ?
        // pages.size() : 0));
        // }

        Map<PageReference, List<String>> pageSnippets = processPages(minToken,
                minTokenPages);

        for (Map.Entry<PageReference, List<String>> entry : pageSnippets.entrySet()) {
            PageReference page = entry.getKey();
            List<String> snippets = entry.getValue();

            System.out.println("Page: " + page.getPageId());
            for (String snippet : snippets) {
                System.out.println("-> Snippet: " + snippet);
            }
            System.out.println();
        }

        // for (String word : originalWords) {
        // System.out.println(word);
        // }
    }
}
