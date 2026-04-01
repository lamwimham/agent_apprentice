package com.keepin.rag.cleaner;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Production-grade document cleaner for RAG (Retrieval-Augmented Generation) pipelines.
 * <p>
 * Provides comprehensive text cleaning operations including:
 * <ul>
 *   <li>Whitespace normalization</li>
 *   <li>HTML/XML tag removal</li>
 *   <li>Special character handling</li>
 *   <li>Unicode normalization</li>
 *   <li>Control character removal</li>
 *   <li>Text structure preservation</li>
 * </ul>
 * </p>
 *
 * @author keepin
 * @version 1.0.0
 */
public class DocumentCleaner {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern MULTIPLE_WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern LEADING_TRAILING_WHITESPACE_PATTERN = Pattern.compile("^\\s+|\\s+$");
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
    private static final Pattern MULTIPLE_NEWLINES_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern HYPHENATED_LINE_BREAK_PATTERN = Pattern.compile("-\\s*\\n\\s*");
    private static final Pattern UNICODE_NORMALIZATION_PATTERN = Pattern.compile("\\p{M}+");
    private static final Pattern EMPTY_LINES_PATTERN = Pattern.compile("(?m)^[ \\t]*\\r?\\n", Pattern.MULTILINE);

    private static final String SPACE = " ";
    private static final String NEWLINE = "\n";
    private static final String EMPTY_STRING = "";

    /**
     * Cleaning options for fine-grained control over the cleaning process.
     */
    public enum CleanOption {
        /** Remove HTML/XML tags */
        REMOVE_HTML_TAGS,
        /** Normalize whitespace (multiple spaces to single) */
        NORMALIZE_WHITESPACE,
        /** Remove control characters */
        REMOVE_CONTROL_CHARS,
        /** Normalize multiple newlines to double newlines */
        NORMALIZE_NEWLINES,
        /** Remove empty lines */
        REMOVE_EMPTY_LINES,
        /** Join hyphenated line breaks (e.g., "exam-\nple" -> "example") */
        JOIN_HYPHENATED_LINES,
        /** Trim leading/trailing whitespace */
        TRIM,
        /** Normalize Unicode characters */
        NORMALIZE_UNICODE,
        /** Remove zero-width characters */
        REMOVE_ZERO_WIDTH,
        /** Apply all cleaning operations */
        ALL
    }

    /**
     * Performs basic text cleaning - whitespace normalization only.
     *
     * @param text the input text to clean
     * @return cleaned text with normalized whitespace
     */
    public static String clean(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return normalizeWhitespace(text);
    }

    /**
     * Performs comprehensive document cleaning with all default options.
     * <p>
     * Default cleaning includes:
     * <ul>
     *   <li>HTML tag removal</li>
     *   <li>Control character removal</li>
     *   <li>Whitespace normalization</li>
     *   <li>Newline normalization</li>
     *   <li>Trimming</li>
     * </ul>
     *
     * @param text the input text to clean
     * @return comprehensively cleaned text
     */
    public static String cleanAll(String text) {
        return clean(text, CleanOption.ALL);
    }

    /**
     * Cleans a list of Spring AI Document objects with all default cleaning options.
     * <p>
     * This method processes each document's text content, removing invalid characters,
     * normalizing whitespace, and cleaning up the text while preserving metadata.
     *
     * @param documents the list of documents to clean
     * @return a new list of documents with cleaned text content
     * @throws IllegalArgumentException if documents is null
     */
    public static List<Document> cleanDocuments(List<Document> documents) {
        return cleanDocuments(documents, CleanOption.ALL);
    }

    /**
     * Cleans a list of Spring AI Document objects with specified cleaning options.
     * <p>
     * This method processes each document's text content according to the specified
     * options while preserving all metadata.
     *
     * @param documents the list of documents to clean
     * @param options   variable number of cleaning options to apply
     * @return a new list of documents with cleaned text content
     * @throws IllegalArgumentException if documents is null
     */
    public static List<Document> cleanDocuments(List<Document> documents, CleanOption... options) {
        if (documents == null) {
            throw new IllegalArgumentException("Documents list cannot be null");
        }

        List<Document> cleanedDocuments = new ArrayList<>(documents.size());
        for (Document document : documents) {
            Document cleanedDocument = cleanDocument(document, options);
            cleanedDocuments.add(cleanedDocument);
        }
        return cleanedDocuments;
    }

    /**
     * Cleans a single Spring AI Document with all default cleaning options.
     * <p>
     * Preserves all metadata while cleaning the text content.
     *
     * @param document the document to clean
     * @return a new document with cleaned text content
     * @throws IllegalArgumentException if document is null
     */
    public static Document cleanDocument(Document document) {
        return cleanDocument(document, CleanOption.ALL);
    }

    /**
     * Cleans a single Spring AI Document with specified cleaning options.
     * <p>
     * Preserves all metadata while cleaning the text content according to
     * the specified options.
     *
     * @param document the document to clean
     * @param options  variable number of cleaning options to apply
     * @return a new document with cleaned text content
     * @throws IllegalArgumentException if document is null
     */
    public static Document cleanDocument(Document document, CleanOption... options) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        String originalText = document.getText();
        String cleanedText = clean(originalText, options);

        // Preserve metadata from the original document, or use empty map if null
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }

        // Create a new document with cleaned text and preserved metadata
        return new Document(cleanedText, metadata);
    }

    /**
     * Performs text cleaning with specified options.
     *
     * @param text the input text to clean
     * @param options variable number of cleaning options to apply
     * @return cleaned text according to specified options
     */
    public static String clean(String text, CleanOption... options) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        boolean applyAll = false;
        boolean removeHtml = false;
        boolean normalizeWs = false;
        boolean removeControl = false;
        boolean normalizeNl = false;
        boolean removeEmpty = false;
        boolean joinHyphen = false;
        boolean trim = false;
        boolean normalizeUni = false;
        boolean removeZeroWidth = false;

        for (CleanOption option : options) {
            switch (option) {
                case ALL:
                    applyAll = true;
                    break;
                case REMOVE_HTML_TAGS:
                    removeHtml = true;
                    break;
                case NORMALIZE_WHITESPACE:
                    normalizeWs = true;
                    break;
                case REMOVE_CONTROL_CHARS:
                    removeControl = true;
                    break;
                case NORMALIZE_NEWLINES:
                    normalizeNl = true;
                    break;
                case REMOVE_EMPTY_LINES:
                    removeEmpty = true;
                    break;
                case JOIN_HYPHENATED_LINES:
                    joinHyphen = true;
                    break;
                case TRIM:
                    trim = true;
                    break;
                case NORMALIZE_UNICODE:
                    normalizeUni = true;
                    break;
                case REMOVE_ZERO_WIDTH:
                    removeZeroWidth = true;
                    break;
            }
        }

        if (applyAll) {
            return applyAllCleaning(text);
        }

        String result = text;

        if (removeHtml) {
            result = removeHtmlTags(result);
        }

        if (removeControl) {
            result = removeControlCharacters(result);
        }

        if (removeZeroWidth) {
            result = removeZeroWidthCharacters(result);
        }

        if (normalizeUni) {
            result = normalizeUnicode(result);
        }

        if (joinHyphen) {
            result = joinHyphenatedLineBreaks(result);
        }

        if (normalizeNl) {
            result = normalizeNewlines(result);
        }

        if (removeEmpty) {
            result = removeEmptyLines(result);
        }

        if (normalizeWs) {
            result = normalizeWhitespace(result);
        }

        if (trim) {
            result = trim(result);
        }

        return result;
    }

    /**
     * Removes HTML and XML tags from the text.
     *
     * @param text the input text
     * @return text with HTML/XML tags removed
     */
    public static String removeHtmlTags(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return HTML_TAG_PATTERN.matcher(text).replaceAll(EMPTY_STRING);
    }

    /**
     * Normalizes whitespace by converting multiple consecutive whitespace
     * characters to a single space.
     *
     * @param text the input text
     * @return text with normalized whitespace
     */
    public static String normalizeWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return MULTIPLE_WHITESPACE_PATTERN.matcher(text).replaceAll(SPACE);
    }

    /**
     * Removes leading and trailing whitespace from the text.
     *
     * @param text the input text
     * @return trimmed text
     */
    public static String trim(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return LEADING_TRAILING_WHITESPACE_PATTERN.matcher(text).replaceAll(EMPTY_STRING);
    }

    /**
     * Removes ASCII control characters (except tab, newline, carriage return).
     *
     * @param text the input text
     * @return text with control characters removed
     */
    public static String removeControlCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return CONTROL_CHAR_PATTERN.matcher(text).replaceAll(EMPTY_STRING);
    }

    /**
     * Normalizes multiple consecutive newlines to exactly two newlines,
     * preserving paragraph structure.
     *
     * @param text the input text
     * @return text with normalized newlines
     */
    public static String normalizeNewlines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String normalized = MULTIPLE_NEWLINES_PATTERN.matcher(text).replaceAll(NEWLINE + NEWLINE);
        normalized = normalized.replaceAll("\\r\\n", NEWLINE);
        normalized = normalized.replaceAll("\\r", NEWLINE);
        return normalized;
    }

    /**
     * Removes empty lines from the text.
     *
     * @param text the input text
     * @return text with empty lines removed
     */
    public static String removeEmptyLines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return EMPTY_LINES_PATTERN.matcher(text).replaceAll(EMPTY_STRING);
    }

    /**
     * Joins words that are split by hyphenated line breaks.
     * For example: "exam-\nple" becomes "example".
     *
     * @param text the input text
     * @return text with hyphenated line breaks joined
     */
    public static String joinHyphenatedLineBreaks(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return HYPHENATED_LINE_BREAK_PATTERN.matcher(text).replaceAll(EMPTY_STRING);
    }

    /**
     * Removes zero-width characters (zero-width space, zero-width non-joiner, etc.).
     *
     * @param text the input text
     * @return text with zero-width characters removed
     */
    public static String removeZeroWidthCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.replaceAll("[\\u200B-\\u200F\\uFEFF\\u2060\\u200A-\\u200E]", EMPTY_STRING);
    }

    /**
     * Normalizes Unicode characters by removing combining diacritical marks.
     *
     * @param text the input text
     * @return text with normalized Unicode
     */
    public static String normalizeUnicode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return UNICODE_NORMALIZATION_PATTERN.matcher(normalized).replaceAll(EMPTY_STRING);
    }

    /**
     * Decodes common HTML entities in the text.
     *
     * @param text the input text
     * @return text with HTML entities decoded
     */
    public static String decodeHtmlEntities(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text
                .replaceAll("&nbsp;", SPACE)
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("&apos;", "'")
                .replaceAll("&ndash;", "–")
                .replaceAll("&mdash;", "—")
                .replaceAll("&lsquo;", "'")
                .replaceAll("&rsquo;", "'")
                .replaceAll("&ldquo;", "\"")
                .replaceAll("&rdquo;", "\"")
                .replaceAll("&hellip;", "…")
                .replaceAll("&bull;", "•")
                .replaceAll("&copy;", "©")
                .replaceAll("&reg;", "®")
                .replaceAll("&trade;", "™");
    }

    /**
     * Applies all available cleaning operations in an optimized order.
     *
     * @param text the input text
     * @return fully cleaned text
     */
    private static String applyAllCleaning(String text) {
        String result = text;

        result = removeHtmlTags(result);
        result = decodeHtmlEntities(result);
        result = removeControlCharacters(result);
        result = removeZeroWidthCharacters(result);
        result = joinHyphenatedLineBreaks(result);
        result = normalizeNewlines(result);
        result = normalizeWhitespace(result);
        result = trim(result);

        return result;
    }

    /**
     * Builder class for creating custom cleaning pipelines.
     */
    public static class Builder {
        private final java.util.EnumSet<CleanOption> options = java.util.EnumSet.noneOf(CleanOption.class);

        public Builder withHtmlTagRemoval() {
            options.add(CleanOption.REMOVE_HTML_TAGS);
            return this;
        }

        public Builder withWhitespaceNormalization() {
            options.add(CleanOption.NORMALIZE_WHITESPACE);
            return this;
        }

        public Builder withControlCharRemoval() {
            options.add(CleanOption.REMOVE_CONTROL_CHARS);
            return this;
        }

        public Builder withNewlineNormalization() {
            options.add(CleanOption.NORMALIZE_NEWLINES);
            return this;
        }

        public Builder withEmptyLineRemoval() {
            options.add(CleanOption.REMOVE_EMPTY_LINES);
            return this;
        }

        public Builder withHyphenatedLineJoining() {
            options.add(CleanOption.JOIN_HYPHENATED_LINES);
            return this;
        }

        public Builder withTrimming() {
            options.add(CleanOption.TRIM);
            return this;
        }

        public Builder withUnicodeNormalization() {
            options.add(CleanOption.NORMALIZE_UNICODE);
            return this;
        }

        public Builder withZeroWidthRemoval() {
            options.add(CleanOption.REMOVE_ZERO_WIDTH);
            return this;
        }

        public Builder withAll() {
            options.add(CleanOption.ALL);
            return this;
        }

        public Builder withOptions(CleanOption... customOptions) {
            for (CleanOption option : customOptions) {
                if (option != CleanOption.ALL) {
                    options.add(option);
                }
            }
            return this;
        }

        public String build(String text) {
            CleanOption[] optionsArray = options.toArray(new CleanOption[0]);
            return clean(text, optionsArray);
        }
    }

    /**
     * Creates a new builder for custom cleaning pipelines.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
