package com.project.searchengine.queryprocessor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.indexer.StopWordFilter;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Component
public class QueryTokenizer {

    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer = new PorterStemmer();
    TokenizerME tokenizer;

    @Autowired
    private PhraseMatcher phraseMatcher;

    public QueryTokenizer(StopWordFilter stopWordFilter) {
        this.stopWordFilter = stopWordFilter;
        loadTokenizerModel();
    }

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
     * Query processing matches the indexer to ensure queries & indexed documents
     * match
     * 
     * 1. converts query to lower case
     * 2. tokenizes query using OpenNLP's SimpleTokenizer
     * 3. Removes stop words using StopWordFilter
     * 4. porter stemming on each token
     * 5. removes any remaining non-alphabetics
     * 
     * @param query: the search query itself
     * @return List of cleaned tokens ready for search
     */
    public QueryResult tokenizeQuery(String query) {
        List<String> tokenizedQuery = new ArrayList<>();
        Map<String, String> tokenizedToOriginal = new HashMap<>();

        query = query.toLowerCase();
        String tokens[] = tokenizer.tokenize(query);
        List<String> originalWords = new ArrayList<>();

        boolean isPhraseMatch = phraseMatcher.isPhraseMatchQuery(query);

        for (String token : tokens) {
            String originalWord = token;

            if (stopWordFilter.isStopWord(token)) {
                continue;
            }

            if (isPhraseMatch) {
                originalWord = originalWord.replace("\"", "");
            }

            // Clean the token for processing
            String processedToken = token.replaceAll("[^a-z]", "");

            // Only add to originalWords if it's not empty after quote removal
            if (!originalWord.isEmpty() && !originalWord.equals("\"")) {
                originalWords.add(originalWord);
            }

            processedToken = stemmer.stem(processedToken);
            
            if (!processedToken.isEmpty()) {
                tokenizedToOriginal.put(processedToken, originalWord);
                tokenizedQuery.add(processedToken);
            }
        }

        System.out.println("Original tokens: " + originalWords);

        return new QueryResult(tokenizedQuery, tokenizedToOriginal, originalWords, isPhraseMatch);
    }
}
