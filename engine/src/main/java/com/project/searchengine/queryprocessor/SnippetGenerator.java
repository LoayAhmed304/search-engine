package com.project.searchengine.queryprocessor;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class SnippetGenerator {
    private static Integer snippetSize = 100;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private final PorterStemmer stemmer = new PorterStemmer();

    @Autowired
    private PhraseMatcher phraseMatcher;

    @Autowired
    private PageRepository pageRepository;

    private String generateSnippet(String[] bodyTokens, int matchPosition, int snippetSize, 
                                  boolean isPhraseMatch, List<String> queryWords) {
        int halfSnippet = snippetSize >>> 2;

        int startIndex = Math.max(0, matchPosition - halfSnippet);
        int endIndex = Math.min(bodyTokens.length, matchPosition + halfSnippet);

        StringBuilder snippet = new StringBuilder();

        String openningPunctuation = "[\\(\\[\\{]";
        String closingPunctuation = "[.,!?;:'\"'/)\\]\\\\]";
        
        // Calculate the range of tokens to highlight if it's a phrase match
        Set<Integer> highlightPositions = new HashSet<>();
        highlightPositions.add(matchPosition); // Always highlight the main match position
        
        if (isPhraseMatch && queryWords != null && !queryWords.isEmpty()) {
            // For phrase matching, we need to highlight multiple consecutive words
            int phraseLength = queryWords.size();
            // Check if we have a forward phrase match
            if (matchPosition + phraseLength <= bodyTokens.length) {
                boolean isForwardMatch = true;
                for (int i = 0; i < phraseLength; i++) {
                    String stemmedToken = stemmer.stem(bodyTokens[matchPosition + i].toLowerCase());
                    String stemmedQuery = stemmer.stem(queryWords.get(i).toLowerCase());
                    if (!stemmedToken.equals(stemmedQuery)) {
                        isForwardMatch = false;
                        break;
                    }
                }
                if (isForwardMatch) {
                    for (int i = 0; i < phraseLength; i++) {
                        highlightPositions.add(matchPosition + i);
                    }
                }
            }
            
            // Check if we have a backward phrase match (if forward didn't match)
            if (highlightPositions.size() <= 1 && matchPosition - phraseLength + 1 >= 0) {
                boolean isBackwardMatch = true;
                for (int i = 0; i < phraseLength; i++) {
                    String stemmedToken = stemmer.stem(bodyTokens[matchPosition - phraseLength + 1 + i].toLowerCase());
                    String stemmedQuery = stemmer.stem(queryWords.get(i).toLowerCase());
                    if (!stemmedToken.equals(stemmedQuery)) {
                        isBackwardMatch = false;
                        break;
                    }
                }
                if (isBackwardMatch) {
                    for (int i = 0; i < phraseLength; i++) {
                        highlightPositions.add(matchPosition - phraseLength + 1 + i);
                    }
                }
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            String token = bodyTokens[i];
            String nextToken = (i + 1 < bodyTokens.length) ? bodyTokens[i + 1] : null;

            boolean isOpeningPunctuation = token.matches(openningPunctuation);
            boolean isClosingNextPunctuation = (nextToken != null) && nextToken.matches(closingPunctuation);

            // Check if current token should be highlighted
            boolean shouldHighlight = highlightPositions.contains(i);
            
            if (shouldHighlight) {
                snippet.append("<strong>").append(token).append("</strong>");
            } else {
                snippet.append(token);
            }
            
            // Add spacing as needed
            if (!isOpeningPunctuation && !isClosingNextPunctuation) {
                snippet.append(" ");
            }
        }

        return snippet.toString().trim();
    }

    /**
     * Gets the tokenized body contnet of reference page provided 
     * 
     * @param referencePage 
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

    /**
     * @param token A list of tokens from the search query.
     * @return body content tokens
     */
    public Map<PageReference, String> getPagesSnippets(
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

                // exclude header positions for now
                String stemmedToken = stemmer.stem(bodyTokens[pos]);
                if (!stemmedToken.equals(token)) {
                    continue;
                }

                boolean isMatchFound = false;

                if (isPhraseMatch) {
                    isMatchFound = phraseMatcher.isPhraseMatchFound(bodyTokens, originalWords, token, pos);
                }

                if (!isPhraseMatch || isPhraseMatch && isMatchFound) {
                    String snippet = generateSnippet(bodyTokens, pos, snippetSize, isPhraseMatch, originalWords);
                    pageSnippet.put(page, snippet);
                    break;
                }
            }
        }
        return pageSnippet;
    }
}
