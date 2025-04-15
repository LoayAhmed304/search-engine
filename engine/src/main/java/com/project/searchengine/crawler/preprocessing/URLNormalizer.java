package com.project.searchengine.crawler.preprocessing;

import java.net.URI;
import java.net.URISyntaxException;

/*
 * This class is responsible for normalizing the URLs.
 * It will normalize the URLs extracted from the documents.
 */

public class URLNormalizer {

    /**
     * Normalizes a URL by:
     * 1. converting to lowercase
     * 2. removing fragments (#)
     * 3. removing duplicate slashes
     * 4. removing query parameters
     * 
     * @param url The URL to normalize
     * @return Normalized URL, or null if invalid
     */
    public static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);

            // Handle protocol-relative URLs
            String scheme = uri.getScheme();
            if (scheme == null && url.startsWith("//")) {
                uri = new URI("https:" + url);
                scheme = uri.getScheme();
            }

            // Rebuild the URL from parts
            String normalized = new URI(
                    scheme != null ? scheme.toLowerCase() : null,
                    uri.getRawUserInfo(),
                    uri.getHost() != null ? uri.getHost().toLowerCase() : null,
                    uri.getPort(),
                    uri.getRawPath().replaceAll("/{2,}", "/"), // Remove duplicate slashes
                    null, // Remove query
                    null // Remove fragment
            ).toString();

            return normalized.replaceAll("(?<!:)/+$", ""); // Remove trailing slashes except after protocol

        } catch (URISyntaxException e) {
            System.err.println("Invalid URL during normalization: " + url);
            return null;
        }
    }

}
