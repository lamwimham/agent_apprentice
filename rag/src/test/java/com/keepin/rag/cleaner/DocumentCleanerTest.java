package com.keepin.rag.cleaner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentCleaner.
 */
class DocumentCleanerTest {

    private static final Map<String, Object> EMPTY_METADATA = new HashMap<>();

    @Test
    @DisplayName("Basic clean - whitespace normalization")
    void testBasicClean() {
        String input = "Hello    world\t\ttest\n\nnew";
        String result = DocumentCleaner.clean(input);
        assertEquals("Hello world test new", result);
    }

    @Test
    @DisplayName("Clean with null input")
    void testCleanNull() {
        assertNull(DocumentCleaner.clean(null));
    }

    @Test
    @DisplayName("Clean with empty string")
    void testCleanEmpty() {
        assertEquals("", DocumentCleaner.clean(""));
    }

    @Test
    @DisplayName("Remove HTML tags")
    void testRemoveHtmlTags() {
        String input = "<p>Hello <b>world</b></p><div>Test</div>";
        String result = DocumentCleaner.removeHtmlTags(input);
        assertEquals("Hello worldTest", result);
    }

    @Test
    @DisplayName("Remove HTML tags with attributes")
    void testRemoveHtmlTagsWithAttributes() {
        String input = "<div class=\"container\"><a href=\"#\">Link</a></div>";
        String result = DocumentCleaner.removeHtmlTags(input);
        assertEquals("Link", result);
    }

    @Test
    @DisplayName("Normalize whitespace")
    void testNormalizeWhitespace() {
        String input = "Multiple   spaces\tand\ttabs";
        String result = DocumentCleaner.normalizeWhitespace(input);
        assertEquals("Multiple spaces and tabs", result);
    }

    @Test
    @DisplayName("Trim whitespace")
    void testTrim() {
        String input = "   leading and trailing   ";
        String result = DocumentCleaner.trim(input);
        assertEquals("leading and trailing", result);
    }

    @Test
    @DisplayName("Remove control characters")
    void testRemoveControlCharacters() {
        String input = "Hello\u0000World\u001FTest";
        String result = DocumentCleaner.removeControlCharacters(input);
        assertEquals("HelloWorldTest", result);
    }

    @Test
    @DisplayName("Normalize newlines")
    void testNormalizeNewlines() {
        String input = "Para1\r\n\r\n\r\nPara2\r\rPara3";
        String result = DocumentCleaner.normalizeNewlines(input);
        // \r\r becomes \n\n (two consecutive carriage returns become two newlines)
        assertEquals("Para1\n\nPara2\n\nPara3", result);
    }

    @Test
    @DisplayName("Remove empty lines")
    void testRemoveEmptyLines() {
        String input = "Line1\n\n\nLine2\n   \nLine3";
        String result = DocumentCleaner.removeEmptyLines(input);
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    @DisplayName("Join hyphenated line breaks")
    void testJoinHyphenatedLineBreaks() {
        String input = "This is an exam-\nple of hyphenation";
        String result = DocumentCleaner.joinHyphenatedLineBreaks(input);
        assertEquals("This is an example of hyphenation", result);
    }

    @Test
    @DisplayName("Remove zero-width characters")
    void testRemoveZeroWidthCharacters() {
        String input = "Hello\u200BWorld\uFEFFTest";
        String result = DocumentCleaner.removeZeroWidthCharacters(input);
        assertEquals("HelloWorldTest", result);
    }

    @Test
    @DisplayName("Decode HTML entities")
    void testDecodeHtmlEntities() {
        String input = "Hello &nbsp; World &amp; &lt;Test&gt; &quot;quoted&quot;";
        String result = DocumentCleaner.decodeHtmlEntities(input);
        // Input: "Hello " + "&nbsp;" + " World" → "Hello " + " " + " World" = "Hello   World" (3 spaces)
        assertEquals("Hello   World & <Test> \"quoted\"", result);
    }

    @Test
    @DisplayName("Clean all operations")
    void testCleanAll() {
        String input = "<p>Hello&nbsp;&nbsp;&nbsp;World</p>\r\n\r\n<div>Test</div>";
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("Test"));
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }

    @Test
    @DisplayName("Clean with specific options")
    void testCleanWithOptions() {
        String input = "<p>Hello   World</p>";
        String result = DocumentCleaner.clean(input, DocumentCleaner.CleanOption.REMOVE_HTML_TAGS);
        assertEquals("Hello   World", result);
    }

    @Test
    @DisplayName("Clean with multiple options")
    void testCleanWithMultipleOptions() {
        String input = "<p>Hello   World</p>";
        String result = DocumentCleaner.clean(input,
                DocumentCleaner.CleanOption.REMOVE_HTML_TAGS,
                DocumentCleaner.CleanOption.NORMALIZE_WHITESPACE);
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("Builder pattern - single option")
    void testBuilderSingleOption() {
        String input = "<p>Hello World</p>";
        String result = DocumentCleaner.builder()
                .withHtmlTagRemoval()
                .build(input);
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("Builder pattern - multiple options")
    void testBuilderMultipleOptions() {
        String input = "<p>Hello   World</p>  ";
        String result = DocumentCleaner.builder()
                .withHtmlTagRemoval()
                .withWhitespaceNormalization()
                .withTrimming()
                .build(input);
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("Builder pattern - all options")
    void testBuilderAllOptions() {
        String input = "<p>Hello&nbsp;World</p>\r\n\r\n<div>Test</div>";
        String result = DocumentCleaner.builder()
                .withAll()
                .build(input);
        assertFalse(result.contains("<"));
        assertTrue(result.contains("Hello"));
    }

    @Test
    @DisplayName("Builder pattern - custom options")
    void testBuilderCustomOptions() {
        String input = "<p>Hello   World</p>";
        String result = DocumentCleaner.builder()
                .withOptions(DocumentCleaner.CleanOption.REMOVE_HTML_TAGS,
                        DocumentCleaner.CleanOption.TRIM)
                .build(input);
        assertEquals("Hello   World", result);
    }

    @Test
    @DisplayName("Preserve paragraph structure with cleanAll")
    void testPreserveParagraphStructure() {
        String input = "<p>First paragraph.</p>\n\n<p>Second paragraph.</p>";
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("First paragraph."));
        assertTrue(result.contains("Second paragraph."));
    }

    @Test
    @DisplayName("Handle complex HTML document")
    void testComplexHtmlDocument() {
        String input = """
                <html>
                <head><title>Test</title></head>
                <body>
                    <div class="content">
                        <h1>Hello World</h1>
                        <p>This is a&nbsp;test.</p>
                        <ul>
                            <li>Item 1</li>
                            <li>Item 2</li>
                        </ul>
                    </div>
                </body>
                </html>
                """;
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("This is a test."));
        assertTrue(result.contains("Item 1"));
        assertFalse(result.contains("<"));
    }

    @Test
    @DisplayName("Handle markdown-style content")
    void testMarkdownContent() {
        String input = """
                # Heading

                This is **bold** and _italic_ text.

                - List item 1
                - List item 2

                [Link](url)
                """;
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("# Heading"));
        assertTrue(result.contains("This is **bold**"));
    }

    @Test
    @DisplayName("Handle code snippets")
    void testCodeSnippets() {
        String input = """
                Here's some code:

                ```java
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hello");
                    }
                }
                ```

                End of code.
                """;
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("public class Test"));
        assertTrue(result.contains("End of code."));
    }

    @Test
    @DisplayName("Handle mixed content")
    void testMixedContent() {
        String input = "Hello\u200B World  \t  <script>alert('xss')</script>Normal text";
        String result = DocumentCleaner.cleanAll(input);
        // HTML tags are removed by cleanAll, including script tags
        assertEquals("Hello World alert('xss')Normal text", result);
    }

    @Test
    @DisplayName("Preserve intentional multiple spaces in context")
    void testIntentionalSpacing() {
        String input = "A    B";
        String result = DocumentCleaner.normalizeWhitespace(input);
        assertEquals("A B", result);
    }

    @Test
    @DisplayName("Handle Windows line endings")
    void testWindowsLineEndings() {
        String input = "Line1\r\nLine2\r\nLine3";
        String result = DocumentCleaner.normalizeNewlines(input);
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    @DisplayName("Handle old Mac line endings")
    void testOldMacLineEndings() {
        String input = "Line1\rLine2\rLine3";
        String result = DocumentCleaner.normalizeNewlines(input);
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    @DisplayName("Handle Unicode combining characters")
    void testUnicodeCombiningCharacters() {
        // Using explicit combining character: e + combining acute accent
        String input = "cafe\u0301"; // "cafe" + combining acute accent
        String result = DocumentCleaner.normalizeUnicode(input);
        // After NFD normalization and removing combining marks, should be "cafe"
        assertEquals("cafe", result);
    }

    @Test
    @DisplayName("Empty result after cleaning")
    void testEmptyResultAfterCleaning() {
        String input = "   \t\n\r   ";
        String result = DocumentCleaner.cleanAll(input);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Handle very long text")
    void testVeryLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("<p>Paragraph ").append(i).append("</p>\n\n");
        }
        String input = sb.toString();
        String result = DocumentCleaner.cleanAll(input);
        assertTrue(result.contains("Paragraph 0"));
        assertTrue(result.contains("Paragraph 999"));
        assertFalse(result.contains("<"));
    }

    // ==================== Document cleaning tests ====================

    @Test
    @DisplayName("Clean single Document with default options")
    void testCleanDocumentDefault() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test.txt");
        metadata.put("page", 1);

        Document document = new Document("Hello   World\t\tTest", metadata);
        Document cleaned = DocumentCleaner.cleanDocument(document);

        assertEquals("Hello World Test", cleaned.getText());
        assertEquals("test.txt", cleaned.getMetadata().get("source"));
        assertEquals(1, cleaned.getMetadata().get("page"));
    }

    @Test
    @DisplayName("Clean single Document with specific options")
    void testCleanDocumentWithOptions() {
        Document document = new Document("<p>Hello   World</p>", EMPTY_METADATA);
        Document cleaned = DocumentCleaner.cleanDocument(document,
                DocumentCleaner.CleanOption.REMOVE_HTML_TAGS,
                DocumentCleaner.CleanOption.NORMALIZE_WHITESPACE);

        assertEquals("Hello World", cleaned.getText());
    }

    @Test
    @DisplayName("Clean Document with empty metadata")
    void testCleanDocumentEmptyMetadata() {
        Document document = new Document("Test   Content", EMPTY_METADATA);
        Document cleaned = DocumentCleaner.cleanDocument(document);

        assertEquals("Test Content", cleaned.getText());
    }

    @Test
    @DisplayName("Clean Document throws on null input")
    void testCleanDocumentNullInput() {
        assertThrows(IllegalArgumentException.class, () -> DocumentCleaner.cleanDocument(null));
    }

    @Test
    @DisplayName("Clean Documents list with default options")
    void testCleanDocumentsDefault() {
        Document doc1 = new Document("Hello   World", EMPTY_METADATA);
        Document doc2 = new Document("Test\t\tContent", EMPTY_METADATA);
        List<Document> documents = Arrays.asList(doc1, doc2);

        List<Document> cleaned = DocumentCleaner.cleanDocuments(documents);

        assertEquals(2, cleaned.size());
        assertEquals("Hello World", cleaned.get(0).getText());
        assertEquals("Test Content", cleaned.get(1).getText());
    }

    @Test
    @DisplayName("Clean Documents list with specific options")
    void testCleanDocumentsWithOptions() {
        Document doc1 = new Document("<p>Hello   World</p>", EMPTY_METADATA);
        Document doc2 = new Document("<div>Test\t\tContent</div>", EMPTY_METADATA);
        List<Document> documents = Arrays.asList(doc1, doc2);

        List<Document> cleaned = DocumentCleaner.cleanDocuments(documents,
                DocumentCleaner.CleanOption.REMOVE_HTML_TAGS,
                DocumentCleaner.CleanOption.NORMALIZE_WHITESPACE);

        assertEquals(2, cleaned.size());
        assertEquals("Hello World", cleaned.get(0).getText());
        assertEquals("Test Content", cleaned.get(1).getText());
    }

    @Test
    @DisplayName("Clean Documents preserves metadata")
    void testCleanDocumentsPreservesMetadata() {
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("filename", "doc1.txt");
        metadata1.put("size", 100L);

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("filename", "doc2.txt");
        metadata2.put("author", "test");

        Document doc1 = new Document("Content   1", metadata1);
        Document doc2 = new Document("Content   2", metadata2);
        List<Document> documents = Arrays.asList(doc1, doc2);

        List<Document> cleaned = DocumentCleaner.cleanDocuments(documents);

        assertEquals(2, cleaned.size());
        assertEquals("Content 1", cleaned.get(0).getText());
        assertEquals("doc1.txt", cleaned.get(0).getMetadata().get("filename"));
        assertEquals(100L, cleaned.get(0).getMetadata().get("size"));

        assertEquals("Content 2", cleaned.get(1).getText());
        assertEquals("doc2.txt", cleaned.get(1).getMetadata().get("filename"));
        assertEquals("test", cleaned.get(1).getMetadata().get("author"));
    }

    @Test
    @DisplayName("Clean Documents with empty list")
    void testCleanDocumentsEmptyList() {
        List<Document> cleaned = DocumentCleaner.cleanDocuments(Collections.emptyList());
        assertTrue(cleaned.isEmpty());
    }

    @Test
    @DisplayName("Clean Documents throws on null input")
    void testCleanDocumentsNullInput() {
        assertThrows(IllegalArgumentException.class, () -> DocumentCleaner.cleanDocuments(null));
    }

    @Test
    @DisplayName("Clean Documents with HTML content")
    void testCleanDocumentsWithHtml() {
        Document doc = new Document("<html><body><p>Hello&nbsp;World</p></body></html>", EMPTY_METADATA);
        List<Document> cleaned = DocumentCleaner.cleanDocuments(Collections.singletonList(doc));

        assertEquals("Hello World", cleaned.get(0).getText());
    }

    @Test
    @DisplayName("Clean Documents with control characters")
    void testCleanDocumentsWithControlCharacters() {
        Document doc = new Document("Hello\u0000World\u001FTest", EMPTY_METADATA);
        List<Document> cleaned = DocumentCleaner.cleanDocuments(Collections.singletonList(doc));

        assertEquals("HelloWorldTest", cleaned.get(0).getText());
    }

    @Test
    @DisplayName("Clean Documents with zero-width characters")
    void testCleanDocumentsWithZeroWidth() {
        Document doc = new Document("Hello\u200BWorld\uFEFFTest", EMPTY_METADATA);
        List<Document> cleaned = DocumentCleaner.cleanDocuments(Collections.singletonList(doc));

        assertEquals("HelloWorldTest", cleaned.get(0).getText());
    }

    @Test
    @DisplayName("Clean Documents with mixed content")
    void testCleanDocumentsMixedContent() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "web");

        Document doc = new Document(
                "<div>Hello&nbsp;&nbsp;World</div>\r\n\r\n<p>Test\u200BContent</p>",
                metadata
        );

        List<Document> cleaned = DocumentCleaner.cleanDocuments(Collections.singletonList(doc));

        String text = cleaned.get(0).getText();
        assertTrue(text.contains("Hello World"));
        assertTrue(text.contains("TestContent"));
        assertFalse(text.contains("<"));
        assertEquals("web", cleaned.get(0).getMetadata().get("source"));
    }

    @Test
    @DisplayName("Clean Document with empty text")
    void testCleanDocumentEmptyText() {
        Document doc = new Document("", EMPTY_METADATA);
        Document cleaned = DocumentCleaner.cleanDocument(doc);

        assertEquals("", cleaned.getText());
    }

    @Test
    @DisplayName("Clean Document with whitespace only")
    void testCleanDocumentWhitespaceOnly() {
        Document doc = new Document("   \t\n\r   ", EMPTY_METADATA);
        Document cleaned = DocumentCleaner.cleanDocument(doc);

        assertEquals("", cleaned.getText());
    }
}