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
        return query.matches("^\".{1,}\"$");
    }

    /**
     * @param tokenIndex
     * @param querySize  length of query
     * @return whether the tokenIndex is a valid index or not
     */
    private boolean isValidTokenIndex(int tokenIndex, int querySize) {
        return tokenIndex >= 0 && tokenIndex < querySize;
    }

    /**
     * @param tokenIndex
     * @param bodyTokensLength
     * @return whether the offset is a valid index or not
     */
    private boolean isValidOffset(int offset, int bodyTokensLength) {
        return offset >= 0 && offset < bodyTokensLength;
    }

    /**
     * Find if the words before or after a specific token (depending on
     * isBeforeToken) match or not
     * 
     * @param bodyTokens
     * @param originalWords list of the query original words before tokenization
     * @param tokenIndex
     * @param isBeforeToken to determine if the match is before the anchor token or
     *                      after it
     * @return true if a match is found
     */

    private boolean findMatchAroundToken(String[] bodyTokens,
            List<String> originalWords,
            int tokenIndex, int pos, boolean isBeforeToken) {

        boolean found = true;
        int offset = isBeforeToken ? pos - 1 : pos + 1;
        int currentTokenIndex = isBeforeToken ? tokenIndex - 1 : tokenIndex + 1;
        int querySize = originalWords.size();

        while (found &&
                isValidOffset(offset, bodyTokens.length)
                && isValidTokenIndex(currentTokenIndex, querySize)) {

            String currentToken = originalWords.get(currentTokenIndex);

            if (!currentToken.equals(bodyTokens[offset])) {
                found = false;
            }

            offset = isBeforeToken ? offset - 1 : offset + 1;
            currentTokenIndex = isBeforeToken ? currentTokenIndex - 1 : currentTokenIndex + 1;
            currentTokenIndex++;
        }

        return found;
    }

    /**
     * Checks if a given token is part of a phrase match in the body content or not
     *
     * @param bodyTokens:
     * @param originalWords: original query words
     * @param token:         token to match
     * @param pos:           position of the current token in body content
     * @return true if the token is part of a matching phrase false otherwise.
     */
    public boolean isPhraseMatchFound(String[] bodyTokens,
            List<String> originalWords, String token, int pos) {

        int tokenIndex = originalWords.indexOf(token);

        if (tokenIndex == -1)
            return false;

        boolean isMatchFound = false;
        boolean isBeforeToken = true;

        isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, isBeforeToken);

        if (isMatchFound) {
            isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, !isBeforeToken);
        }

        return isMatchFound;
    }
}
