package com.project.searchengine.indexer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.tokenattributes.*;

public class Tokenizer {
    private final Analyzer analyzer; // used currently with the default stop words

    /*
     * Constructor for the Tokenizer class
     * Initializes the analyzer with the default stop words
     * and a standard tokenizer
     */
    public Tokenizer() {
        this.analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                StandardTokenizer tokenizer = new StandardTokenizer();
                return new TokenStreamComponents(tokenizer, new LowerCaseFilter(tokenizer));
            }
        };
    }
    // public Tokenizer() {

    // }

    // public Map<String, List<Integer>> tokenizeWithPositions(String text) {
    //     Map<String, List<Integer>> tokens = new HashMap<>();

    //     try (TokenStream stream = analyzer.tokenStream(null, text)) {
    //         while(stream.incrementToken()) {
    //             CharTermAttribute charTermAttribute = stream.getAttribute(CharTermAttribute.class); // token
    //             PositionIncrementAttribute positionIncrementAttribute = stream.getAttribute(PositionIncrementAttribute.class); // position

    //             // Reset the stream to a clean state
    //             // stream.reset();

    //             System.out.println("Token: " + charTermAttribute.toString());
    //             System.out.println("Position: " + positionIncrementAttribute.getPositionIncrement());

    //         }

    //     } catch (Exception e) {
    //         System.out.println("Error in Tokenizer.tokenizeWithPositions: " + e.getMessage());

    //     }


    //     return tokens;
    // }

    public Map<String, List<Integer>> tokenizeWithPositions(String text) {
        Map<String, List<Integer>> tokenPositions = new HashMap<>();
        try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))) {
            CharTermAttribute charTermAttr = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute posIncAttr = stream.addAttribute(PositionIncrementAttribute.class);
            
            System.out.println("Token: " + charTermAttr.toString());
            System.out.println("Position: " + posIncAttr.getPositionIncrement());

            stream.reset();
            int position = 0;
            while (stream.incrementToken()) {
                String token = charTermAttr.toString();
                tokenPositions.computeIfAbsent(token, k -> new ArrayList<>()).add(position);
                position += posIncAttr.getPositionIncrement();

                System.out.println("Token: " + token);
                System.out.println("Position: " + position);
                
            }
            stream.end();
        } catch (IOException e) {
            throw new RuntimeException("Failed to tokenize text: " + e.getMessage(), e);
        }
        return tokenPositions;
    }

   
    public static void main(String[] args) {
        Tokenizer tokenizer = new Tokenizer();
        String text = "This is a test sentence. This is another test sentence.";

        tokenizer.tokenizeWithPositions(text);
        
    }

    
}
