package com.project.searchengine.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtil {
    /**
     * ObjectMapper instance for parsing JSON.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a list of JSON strings, where each string is an object with a
     * 'normalizedUrl' field,
     * into a List of Strings.
     *
     * @param jsonList The list of JSON strings to parse
     * @return List of normalized URLs
     */
    public static List<String> parseNormalizedUrls(List<String> jsonList) {
        List<String> urls = new ArrayList<>();

        if (jsonList == null || jsonList.isEmpty()) {
            return urls;
        }

        for (String json : jsonList) {
            try {
                JsonNode node = objectMapper.readTree(json);
                String normalizedUrl = node.get("normalizedUrl").asText();
                urls.add(normalizedUrl);
            } catch (JsonMappingException e) {
                System.err.println("Mapping error for JSON: " + json + " - " + e.getMessage());
            } catch (JsonProcessingException e) {
                System.err.println("Processing error for JSON: " + json + " - " + e.getMessage());
            }
        }
        return urls;
    }
}