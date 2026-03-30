package com.keepin.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DocumentStrategyFactory {

    private final List<IDocumentReaderStrategy> strategies;

    public DocumentStrategyFactory(List<IDocumentReaderStrategy> strategies) {
        this.strategies = strategies;
    }

    public List<Document> read(File file) throws Exception {
        for (IDocumentReaderStrategy strategy : strategies) {
            if (strategy.supports(file)) {
                return strategy.read(file);
            }
        }
        throw new Exception("No strategy found for file: " + file);
    }
}

