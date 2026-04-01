package com.keepin.rag.controller;

import com.keepin.rag.cleaner.DocumentCleaner;
import com.keepin.rag.reader.DocumentStrategyFactory;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagReaderController {

    private final DocumentStrategyFactory documentStrategyFactory;

    public RagReaderController(DocumentStrategyFactory documentStrategyFactory) {
        this.documentStrategyFactory = documentStrategyFactory;
    }

    @RequestMapping("/read")
    public List<Document> read(String filePath) throws Exception {
        List<Document> documents;

        try {
            documents = DocumentCleaner.cleanDocuments(documentStrategyFactory.read(new File(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Document document : documents) {
            System.out.println(document.getText());
            System.out.println(document.getMetadata());
            System.out.println(document.getMetadata().get("title"));
        }
        return documents;
    }
}
