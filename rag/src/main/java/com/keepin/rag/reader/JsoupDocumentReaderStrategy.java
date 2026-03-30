package com.keepin.rag.reader;


import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class JsoupDocumentReaderStrategy implements  IDocumentReaderStrategy {
    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".html") || name.endsWith(".htm");
    }

    @Override
    public List<Document> read(File file) throws IOException {
        Resource resource = new FileSystemResource(file);
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("p")
                .charset("utf-8")
                .includeLinkUrls(true)
                .metadataTags(List.of("author", "data"))
                .additionalMetadata("filename", file.getName())
                .build();
        return new JsoupDocumentReader(resource, config).read();

    }
}
