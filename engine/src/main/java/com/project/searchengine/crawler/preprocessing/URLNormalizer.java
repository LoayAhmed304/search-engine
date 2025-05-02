package com.project.searchengine.crawler.preprocessing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class URLNormalizer {
    
    /**
     * Normalizes a URL by:
     * 1. Converting to lowercase
     * 2. Removing fragments (#)
     * 3. Removing duplicate slashes
     * 4. Removing query parameters
     * 5. Filtering out non-English URLs
     * 
     * @param url The URL to normalize
     * @return Normalized URL, or null if invalid or non-English
     */
    public static String normalizeUrl(String url) {
        try {
            // Replace unencoded spaces with %20
            String cleanedUrl = url.replace(" ", "%20");

            URI uri = new URI(cleanedUrl);

            // Handle protocol-relative URLs
            String scheme = uri.getScheme();
            if (scheme == null) {
                if (url.startsWith("//")) {
                    uri = new URI("https:" + url);
                    scheme = uri.getScheme();
                } else {
                    System.out.println("No schema provided for: " + url);
                    return null; // Invalid URL
                }
            }

            // Filter out non-English URLs
            if (!isEnglishUrl(uri)) {
                return null;
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
            System.err.println("Invalid URL during normalization: " + url + " | Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a URL is likely to contain English content based on domain.
     * Allows URLs with 'en' subdomain or no language code in subdomain.
     */
    private static boolean isEnglishUrl(URI uri) {
        String host = uri.getHost();
        if (host == null) return false;

        String[] parts = host.split("\\.");
        if (parts.length < 2) return false;

        // Check for language code in subdomain (e.g., en.wikipedia.org)
        String subdomain = parts[0].toLowerCase();
        return subdomain.equals("en") || !isLanguageCode(subdomain);
    }

    /**
     * Checks if a string is a common language code (excluding 'en').
     */
    private static boolean isLanguageCode(String code) {
        Set<String> languageCodes = new HashSet<>(Arrays.asList(
            "aa", "ab", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi",
            "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de",
            "dv", "dz", "ee", "el", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr", "fy", "ga",
            "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id",
            "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl",
            "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt",
            "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "na", "nb", "nd", "ne",
            "ng", "nl", "nn", "no", "nr", "nv", "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps",
            "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk", "sl", "sm",
            "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "ti", "tk", "tl",
            "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo",
            "xh", "yi", "yo", "za", "zh", "zu"
        ));
        return languageCodes.contains(code) && !code.equals("en");
    }
}