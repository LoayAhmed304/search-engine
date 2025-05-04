package com.project.searchengine.indexer;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;
import java.io.InputStream;
import java.util.*;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tokenizer {

    private final Map<String, InvertedIndex> indexBuffer = new HashMap<>();
    private final Map<String, Integer> pagesTokensCount = new HashMap<>();

    private final PorterStemmer stemmer = new PorterStemmer();

    // SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    TokenizerME tokenizer;

    @Autowired
    private final StopWordFilter stopWordFilter;

    public Tokenizer(StopWordFilter stopWordFilter) {
        this.stopWordFilter = stopWordFilter;
        loadTokenizerModel();
    }

    /**
     * Tokenizes the input text and build the inverted index
     * @param text The input text to tokenize
     * @param pageId The current page id
     * @param fieldType The field type (e.g., body, title, h1, h2)
     */
    public void tokenizeContent(String text, String pageId) {
        long startTime = System.currentTimeMillis();
        int position = 0;

        // Tokenize the text
        String tokens[] = tokenizer.tokenize(text.toLowerCase());

        for (String token : tokens) {
            String cleanedToken = cleanToken(token);
            if (!cleanedToken.isEmpty()) {
                // Build the inverted index and add it to the index buffer
                buildInvertedIndex(cleanedToken, pageId, position);

                // Increment token count for page id
                pagesTokensCount.merge(pageId, 1, Integer::sum);
            }
            // Increment position to the next token
            position++;
        }

        long endTime = System.currentTimeMillis();
        System.out.println(
            "Tokenized " + tokens.length + " tokens in " + (endTime - startTime) + " ms"
        );
    }

    /**
     * Tokenizes the headers of the page and updates the field count
     * for each header type (h1, h2, title)
     * @param fieldTags The field tags to tokenize.
     * @param pageId The page id.
     */

    public void tokenizeHeaders(Elements fieldTags, String pageId) {
        for (Element header : fieldTags) {
            String headerText = header.text();
            if (headerText == null || headerText.isBlank()) continue;
            String headerType = header.tagName();

            String tokens[] = tokenizer.tokenize(headerText.toLowerCase());

            for (String token : tokens) {
                String cleanedToken = cleanToken(token);
                if (!cleanedToken.isEmpty()) {
                    //  Update field count for the header type
                    updateFieldCount(cleanedToken, pageId, headerType);
                }
            }
        }
    }

    /**
     * Build the inverted index to be saved to the database
     * If it already exists update it
     *
     * @param word The Id of the inverted index
     * @param pageId The Id of the page that contains the word
     * @param position The position of the word in the page
     * @param fieldType  The field type (e.g., body, title, h1, h2)
     */
    private void buildInvertedIndex(String word, String pageId, Integer position) {
        // Get or create inverted index from buffer
        InvertedIndex invertedIndex = indexBuffer.computeIfAbsent(word, w -> new InvertedIndex(word)
        );

        // Update or create pageReference
        PageReference pageReference = invertedIndex
            .getPages()
            .stream()
            .filter(p -> p.getPageId().equals(pageId))
            .findFirst()
            .orElseGet(() -> {
                PageReference newPageReference = new PageReference(pageId);
                invertedIndex.addPage(newPageReference);
                return newPageReference;
            });

        // Update positions
        pageReference.getWordPositions().add(position);
    }

    /**
     * Update the field count for the given word and page id
     *
     * @param word The word to update.
     * @param pageId The page id.
     * @param fieldType The field type (title, h1, h2).
     */
    public void updateFieldCount(String word, String pageId, String fieldType) {
        // Get the inverted index from the buffer
        InvertedIndex invertedIndex = indexBuffer.get(word);

        if (invertedIndex != null) {
            // Get the page reference
            PageReference pageReference = invertedIndex
                .getPages()
                .stream()
                .filter(p -> p.getPageId().equals(pageId))
                .findFirst()
                .orElse(null);

            if (pageReference != null) {
                // Update the field count
                pageReference.getFieldWordCount().merge(fieldType, 1, Integer::sum);
            }
        }
    }

    /**
     * Cleans the token by removing unwanted characters.
     * Preserves special tokens like email, phone, hashtags, and hyphenated words.
     *
     * @param token The token to clean.
     * @return The cleaned token.
     */
    private String cleanToken(String token) {
        if (stopWordFilter.isStopWord(token)) {
            return "";
        }

        token = token.replaceAll("[^a-z]", "");

        String cleanedToken = stemmer.stem(token);

        // Skip stop words
        if (cleanedToken.length() < 2) {
            return "";
        }

        // Apply stemming to regural words
        return cleanedToken;
    }

    /**
     * Reset the tokenizer for a new batch of documents.
     *
     * This method clears the index buffer and the pages tokens count.
     */
    public void resetForNewBatch() {
        indexBuffer.clear();
        pagesTokensCount.clear();
        System.out.println("Reset tokenizer for new batch");
    }

    /**
     * Load the tokenizer model from the specified input stream.
     *
     * @param modelInputStream The input stream containing the tokenizer model.
     */
    void loadTokenizerModel() {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/en-token.bin")) {
            if (modelIn == null) {
                throw new IllegalArgumentException("Model file not found");
            }
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(model);
        } catch (Exception e) {
            throw new RuntimeException("Error loading tokenizer model", e);
        }
    }

    /**
     * Return index buffer of all tokens of the current document
     */
    public Map<String, InvertedIndex> getIndexBuffer() {
        return indexBuffer;
    }

    /**
     * Set the count of tokens for each page reference in the index buffer
     */
    int getPageTokenCount(String pageId) {
        int tokenCount = pagesTokensCount.getOrDefault(pageId, 0);
        return tokenCount;
    }

    /**
     * Return the count of tokens for each page reference in the index buffer
     */
    Map<String, Integer> getPagesTokensCount() {
        return pagesTokensCount;
    }
}
