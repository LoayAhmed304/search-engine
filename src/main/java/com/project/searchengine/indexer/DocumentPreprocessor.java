package com.project.searchengine.indexer;

import com.project.searchengine.crawler.preprocessing.*;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.service.PageService;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class DocumentPreprocessor {

    @Autowired
    private Tokenizer tokenizer;

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
        // Extract raw text
        String title = document.title();
        String id = hashUrl(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        // Tokenize the document
        tokenizer.tokenizeHeaders(fieldTags, id, 0.0);
        tokenizer.tokenizeContent(content, id, "body", 0.0);

        tokenizer.saveTokens();
    }

    public void savePage(String id, String url, String title, String content) {
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
