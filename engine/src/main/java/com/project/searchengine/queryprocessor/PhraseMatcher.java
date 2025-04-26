package com.project.searchengine.queryprocessor;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PhraseMatcher {
    /**
     * @param query
     * @return whether it is a phrase matching query or not
     */
    public boolean isPhraseMatchQuery(String query) {
        if (query.isEmpty() || query.length() < 2) {
            return false;
        }

        return (query.charAt(0) == '\"'
                && query.charAt(query.length() - 1) == '\"')
                        ? true
                        : false;
    }

    /**
     * Checks if a given token is part of a phrase match in the body content or not
     *
     * @param bodyTokens:  
     * @param originalWords: original query words
     * @param token: token to match
     * @param pos: position of the current token in body content
     * @return true if the token is part of a matching phrase false otherwise.
     */
    public boolean isPhraseMatchFound(String[] bodyTokens,
            List<String> originalWords, String token, int pos) {

        // check positions around it
        int tokenIndex = originalWords.indexOf(token);
        int querySize = originalWords.size();

        // System.out.println(tokenIndex + " " + querySize);

        boolean found = true;

        // check letters before
        int offset = pos - 1;
        int prevTokenIndex = tokenIndex - 1;

        while (found && prevTokenIndex >= 0 && offset >= 0) {
            String prevToken = originalWords.get(prevTokenIndex);

            if (!prevToken.equals(bodyTokens[offset])) {
                found = false;
            }
            offset--;
            prevTokenIndex--;
        }

        offset = pos + 1;
        int nextTokenIndex = tokenIndex + 1;

        while (found && offset < bodyTokens.length && nextTokenIndex < querySize) {
            String nextToken = originalWords.get(nextTokenIndex);

            if (!nextToken.equals(bodyTokens[offset])) {
                found = false;
            }
            offset++;
            nextTokenIndex++;
        }
        return found;
    }
}
