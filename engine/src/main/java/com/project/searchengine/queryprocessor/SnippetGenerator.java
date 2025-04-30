package com.project.searchengine.queryprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
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
    private static Integer snippetSize = 30;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private final PorterStemmer stemmer = new PorterStemmer();

    @Autowired
    private PhraseMatcher phraseMatcher;

    @Autowired
    private PageRepository pageRepository;

    private String generateSnippet(String[] bodyTokens, int matchPosition, int snippetSize) {
        int halfSnippet = snippetSize >>> 2;

        int startIndex = Math.max(0, matchPosition - halfSnippet);
        int endIndex = Math.min(bodyTokens.length, matchPosition + halfSnippet);

        StringBuilder snippet = new StringBuilder();

        String openningPunctuation = "[\\(\\[\\{]";
        String closingPunctuation = "[.,!?;:’\"'/)\\]\\\\]";

        for (int i = startIndex; i < endIndex; i++) {
            String token = bodyTokens[i];
            String nextToken = (i + 1 < bodyTokens.length) ? bodyTokens[i + 1] : null;

            boolean isOpeningPunctuation = token.matches(openningPunctuation);
            boolean isClosingNextPunctuation = (nextToken != null) && nextToken.matches(closingPunctuation);

            if (isOpeningPunctuation || isClosingNextPunctuation) {
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
    private String[] getPageBodyContent(PageReference referencePage) {
        String pageId = referencePage.getPageId();
        Page page = pageRepository.getPageById(pageId);

        String content = page.getContent();
        Document document = Jsoup.parse(content);
        String bodyContent = document.body().text();

        return tokenizer.tokenize(bodyContent.toLowerCase());
    }

    public Map<PageReference, String> getAllPagesSnippet(
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
                    String snippet = generateSnippet(bodyTokens, pos, snippetSize);
                    pageSnippet.put(page, snippet);
                    break;
                }
            }
        }
        return pageSnippet;
    }
}
