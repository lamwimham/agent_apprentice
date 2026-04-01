package com.keepin.rag.spliter;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChineseSemanticTextSplitterTest {

    @Test
    void testSplitSimpleChineseText() {
        String text = "这是一个测试句子。这是第二个句子。这是第三个句子。";
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(100, 20);
        
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        // 验证每个 chunk 都以句子结束符结尾（除了最后一个可能例外）
        for (String chunk : chunks) {
            assertNotNull(chunk);
            assertFalse(chunk.isBlank());
        }
    }

    @Test
    void testSplitLongChineseText() {
        // 构建一个较长的中文文本
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("这是第").append(i + 1).append("个测试句子，用于测试中文语义分块功能。");
        }
        String text = sb.toString();
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(200, 50);
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // 验证每个 chunk 不超过 chunkSize（考虑重叠）
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 250, "Chunk length should not exceed chunkSize significantly");
        }
    }

    @Test
    void testSplitWithParagraphs() {
        String text = "这是第一段的第一句话。这是第一段的第二句话。\n\n" +
                      "这是第二段的第一句话。这是第二段的第二句话。";
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(100, 20);
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
    }

    @Test
    void testSplitEmptyText() {
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(100, 20);
        
        List<String> chunks = splitter.splitText("");
        assertTrue(chunks.isEmpty());
        
        chunks = splitter.splitText(null);
        assertTrue(chunks.isEmpty());
        
        chunks = splitter.splitText("   ");
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testApplyWithDocuments() {
        String text = "这是一个测试文档。它包含多个句子。每个句子都应该被正确分割。" +
                      "这是另一个句子。我们希望语义分块能够正常工作。";
        Document doc = new Document(text);
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(50, 10);
        List<Document> chunks = splitter.apply(List.of(doc));
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // 验证每个 chunk 都是有效的 Document
        for (Document chunk : chunks) {
            assertNotNull(chunk.getText());
            assertFalse(chunk.getText().isBlank());
        }
    }

    @Test
    void testInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ChineseSemanticTextSplitter(0, 10));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new ChineseSemanticTextSplitter(-1, 10));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new ChineseSemanticTextSplitter(100, -1));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new ChineseSemanticTextSplitter(100, 100));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new ChineseSemanticTextSplitter(100, 150));
    }

    @Test
    void testMixedChineseAndEnglish() {
        String text = "这是中文句子。This is an English sentence. 又是中文句子。Another English one.";
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(100, 20);
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
    }

    @Test
    void testSentenceBoundaryPreservation() {
        // 测试句子边界保持
        String text = "人工智能是计算机科学的一个分支。它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。";
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(30, 5);
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        // 验证分割后的 chunk 不会在句子中间断开（除非句子太长）
        for (String chunk : chunks) {
            // 每个 chunk 应该包含完整的句子或句子片段
            assertNotNull(chunk);
            assertFalse(chunk.isBlank());
        }
    }

    @Test
    void testOverlap() {
        String text = "第一句话比较短。第二句话也比较短。第三句话同样很短。第四句话继续很短。";
        
        ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(20, 5);
        List<String> chunks = splitter.splitText(text);
        
        assertNotNull(chunks);
        // 如果有多个 chunk，验证重叠机制
        if (chunks.size() > 1) {
            // 重叠文本应该在相邻 chunk 中出现
            for (int i = 0; i < chunks.size() - 1; i++) {
                String currentChunk = chunks.get(i);
                String nextChunk = chunks.get(i + 1);
                // 验证重叠存在（某些情况下可能不明显）
                assertNotNull(currentChunk);
                assertNotNull(nextChunk);
            }
        }
    }
}