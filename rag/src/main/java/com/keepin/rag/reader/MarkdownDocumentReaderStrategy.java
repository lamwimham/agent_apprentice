package com.keepin.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class MarkdownDocumentReaderStrategy implements IDocumentReaderStrategy {
    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown");
    }

    @Override
    public List<Document> read(File file) throws IOException {
        Resource resource = new FileSystemResource(file);

        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("filename", file.getName())
                .build();
        return new MarkdownDocumentReader(resource, config).read();
    }
}
