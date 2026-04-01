package com.keepin.rag.spliter;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import io.micrometer.common.util.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 中文语义分块器：基于 HanLP 实现中文句子分割，支持语义边界分块
 */
public class ChineseSemanticTextSplitter extends TextSplitter {

    private final int chunkSize;
    private final int overlap;
    private final boolean preserveSentence;

    // 中文句子结束标点
    private static final String SENTENCE_DELIMITERS = "[。！？；;.!?]+";

    public ChineseSemanticTextSplitter(int chunkSize, int overlap) {
        this(chunkSize, overlap, true);
    }

    public ChineseSemanticTextSplitter(int chunkSize, int overlap, boolean preserveSentence) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize 必须大于 0");
        }
        if (overlap < 0) {
            throw new IllegalArgumentException("overlap 不能为负数");
        }
        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap 不能大于等于 chunkSize");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
        this.preserveSentence = preserveSentence;
    }

    /**
     * 将文本分割成句子列表
     */
    private List<String> splitIntoSentences(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        List<String> sentences = new ArrayList<>();
        StringBuilder currentSentence = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            currentSentence.append(c);

            // 检查是否是句子结束符
            if (isSentenceDelimiter(c)) {
                String sentence = currentSentence.toString().trim();
                if (!sentence.isEmpty()) {
                    sentences.add(sentence);
                }
                currentSentence = new StringBuilder();
            }
        }

        // 处理最后一个句子（可能没有结束符）
        String lastSentence = currentSentence.toString().trim();
        if (!lastSentence.isEmpty()) {
            sentences.add(lastSentence);
        }

        return sentences;
    }

    private boolean isSentenceDelimiter(char c) {
        return c == '。' || c == '！' || c == '？' || c == '；' ||
               c == '.' || c == '!' || c == '?' || c == ';';
    }

    /**
     * 基于语义边界分割文本
     */
    public List<String> splitText(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        // 先按段落分割
        String[] paragraphs = text.split("\\n+");
        List<String> allSentences = new ArrayList<>();

        for (String paragraph : paragraphs) {
            if (StringUtils.isBlank(paragraph)) continue;
            // 每个段落按句子分割
            List<String> sentences = splitIntoSentences(paragraph);
            allSentences.addAll(sentences);
        }

        // 按语义边界组合成 chunks
        return buildSemanticChunks(allSentences);
    }

    /**
     * 将句子组合成语义完整的 chunks
     */
    private List<String> buildSemanticChunks(List<String> sentences) {
        List<String> chunks = new ArrayList<>();
        if (CollectionUtils.isEmpty(sentences)) {
            return chunks;
        }

        StringBuilder currentChunk = new StringBuilder();
        List<String> currentSentences = new ArrayList<>();

        for (String sentence : sentences) {
            // 如果单个句子超过 chunkSize，需要进一步分割
            if (sentence.length() > chunkSize) {
                // 先保存当前 chunk
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // 处理重叠
                    currentChunk = new StringBuilder(getOverlapText(chunks.get(chunks.size() - 1)));
                    currentSentences.clear();
                }
                // 分割长句子
                List<String> subChunks = splitLongSentence(sentence);
                chunks.addAll(subChunks);
                // 最后一个子块作为下一个 chunk 的重叠基础
                if (!subChunks.isEmpty()) {
                    currentChunk = new StringBuilder(getOverlapText(subChunks.get(subChunks.size() - 1)));
                }
                continue;
            }

            // 检查添加这个句子是否会超过 chunkSize
            if (currentChunk.length() + sentence.length() + 1 > chunkSize) {
                // 保存当前 chunk
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }
                // 处理重叠
                String overlapText = getOverlapText(currentChunk.toString());
                currentChunk = new StringBuilder();
                if (!overlapText.isEmpty()) {
                    currentChunk.append(overlapText);
                }
                currentSentences.clear();
            }

            // 添加句子到当前 chunk
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
            currentSentences.add(sentence);
        }

        // 处理最后一个 chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 分割长句子（使用 HanLP 分词进行智能分割）
     */
    private List<String> splitLongSentence(String sentence) {
        List<String> subChunks = new ArrayList<>();
        
        if (preserveSentence) {
            // 使用 HanLP 分词，在短语边界分割
            List<Term> terms = HanLP.segment(sentence);
            StringBuilder currentSubChunk = new StringBuilder();

            for (Term term : terms) {
                if (currentSubChunk.length() + term.word.length() > chunkSize) {
                    if (currentSubChunk.length() > 0) {
                        subChunks.add(currentSubChunk.toString().trim());
                        // 添加重叠
                        String overlapText = getOverlapText(currentSubChunk.toString());
                        currentSubChunk = new StringBuilder();
                        if (!overlapText.isEmpty()) {
                            currentSubChunk.append(overlapText).append(" ");
                        }
                    }
                }
                currentSubChunk.append(term.word);
            }

            if (currentSubChunk.length() > 0) {
                subChunks.add(currentSubChunk.toString().trim());
            }
        } else {
            // 简单按字符分割
            for (int i = 0; i < sentence.length(); i += chunkSize - overlap) {
                int end = Math.min(i + chunkSize, sentence.length());
                subChunks.add(sentence.substring(i, end));
            }
        }

        return subChunks;
    }

    /**
     * 获取重叠文本
     */
    private String getOverlapText(String text) {
        if (overlap <= 0 || StringUtils.isBlank(text) || text.length() <= overlap) {
            return "";
        }
        // 尝试在句子边界处截取重叠
        String overlapText = text.substring(text.length() - overlap);
        // 找到第一个句子结束符的位置
        int firstDelimiter = -1;
        for (int i = 0; i < overlapText.length(); i++) {
            if (isSentenceDelimiter(overlapText.charAt(i))) {
                firstDelimiter = i;
                break;
            }
        }
        if (firstDelimiter >= 0 && firstDelimiter < overlapText.length() - 1) {
            return overlapText.substring(firstDelimiter + 1).trim();
        }
        return overlapText;
    }

    /**
     * 批量拆分文档
     */
    public List<Document> apply(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }

        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            List<String> chunks = splitText(doc.getText());
            for (String chunk : chunks) {
                result.add(new Document(chunk, doc.getMetadata()));
            }
        }
        return result;
    }
}