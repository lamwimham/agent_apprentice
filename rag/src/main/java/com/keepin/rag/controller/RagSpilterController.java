package com.keepin.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.keepin.rag.cleaner.DocumentCleaner;
import com.keepin.rag.reader.DocumentStrategyFactory;
import com.keepin.rag.spliter.ChineseSemanticTextSplitter;
import com.keepin.rag.spliter.OverlapParagraphTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagSpilterController {

    private final DocumentStrategyFactory documentStrategyFactory;

    public RagSpilterController(DocumentStrategyFactory documentStrategyFactory) {
        this.documentStrategyFactory = documentStrategyFactory;
    }

    @RequestMapping("/split")
    public String chunker(String filePath) {
        List<Document> documents;

        try {
            documents = DocumentCleaner.cleanDocuments(documentStrategyFactory.read(new File(filePath)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Document document : documents) {
            System.out.println("before chunking: " + document.getText());
            TokenTextSplitter splitter = new TokenTextSplitter(
                    500,
                    300,
                    5,
                    8000,
                    true
            );
            List<Document> chunks = splitter.split(document);
            for (Document chunk : chunks) {
                System.out.println("chunk: " + chunk.getText());
                System.out.println("");
            }
            System.out.println("==============");
        }
        return documents.toString();
    }

    @RequestMapping("/overlap_split")
    public String overlapChunker(String filePath) {
        List<Document> documents;
        try {
            documents = DocumentCleaner.cleanDocuments(documentStrategyFactory.read(new File(filePath)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Document document : documents) {
//            System.out.println("before chunking: " + document.getText());
            OverlapParagraphTextSplitter splitter = new OverlapParagraphTextSplitter(500, 100);
            List<Document> chunks = splitter.apply(List.of(document));
            for (Document chunk : chunks) {
                System.out.println("chunk: " + chunk.getText());
                System.out.println("");
            }
            System.out.println("==============");
        }
        return documents.toString();
    }

    @RequestMapping("/recursive_split")
    public String recursiveChunker(String filePath) {
        List<Document> documents;
        try {
            documents = DocumentCleaner.cleanDocuments(documentStrategyFactory.read(new File(filePath)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Document document : documents) {
            RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(400, new String[] {"\n", "\n\n"});
            List<Document> chunks = splitter.apply(List.of(document));
            for (Document chunk : chunks) {
                System.out.println("after recursive chunking: " + chunk.getText());
                System.out.println("");
            }
            System.out.println("==============");
        }
        return documents.toString();
    }

    @RequestMapping("/chinese_semantic_split")
    public String chineseSemanticChunker(String filePath) {
        List<Document> documents;
        try {
            documents = DocumentCleaner.cleanDocuments(documentStrategyFactory.read(new File(filePath)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Document document : documents) {
            System.out.println("before chinese semantic chunking: " + document.getText());
            System.out.println("========================================");
            ChineseSemanticTextSplitter splitter = new ChineseSemanticTextSplitter(500, 100);
            List<Document> chunks = splitter.apply(List.of(document));
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                System.out.println("chunk " + (i + 1) + " (length=" + chunk.getText().length() + "):");
                System.out.println(chunk.getText());
                System.out.println("----------------------------------------");
            }
            System.out.println("==============");
        }
        return "Chinese semantic split completed. Total chunks: " + documents.size();
    }

}