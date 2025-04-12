package com.project.searchengine.indexer;

import com.project.searchengine.crawler.preprocessing.*;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.service.PageService;
import java.security.MessageDigest;
import java.util.*;
import javax.xml.bind.DatatypeConverter;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class DocumentPreprocessor {

    Tokenizer tokenizer = new Tokenizer();

    @Autowired
    private PageService pageService;

    /**
     * Preprocesses the document by extracting tokens and their positions.
     * It also extracts header tokens and their occurrences.
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     * @return A map containing the tokens and their positions, as well as header tokens. (To be changed)
     */
    public void preprocessDocument(String url, Document document) {
        // Field-specefic tokens
        Map<String, List<Integer>> bodyTokens = new HashMap<>(); // word => list of positions
        Map<String, Map<String, Integer>> headerTokens = new HashMap<>(); // word => header => count of occurrences

        // Extract raw text
        String title = document.title();
        String id = hashUrl(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        // Create the page to be saved in the database
        createPage(id, url, title, content);

        // Tokenize the document
        bodyTokens = tokenizer.tokenizeContent(content);
        headerTokens = tokenizer.tokenizeHeaders(fieldTags);
    }

    public void createPage(String id, String url, String title, String content) {
        Page page = new Page(id, url, title, content);

        pageService.createPage(page);
    }

    private String hashUrl(String url) {
        // Normalize the url first
        String normalizedUrl = URLNormalizer.normalizeUrl(url);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedUrl.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (Exception e) {
            return Integer.toHexString(url.hashCode());
        }
    }
}
