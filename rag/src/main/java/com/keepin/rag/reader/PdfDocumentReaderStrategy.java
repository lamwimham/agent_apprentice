package com.keepin.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class PdfDocumentReaderStrategy implements IDocumentReaderStrategy {
    @Override
    public boolean supports(File file) {
        return file.getName().endsWith(".pdf");
    }

    @Override
    public List<Document> read(File file) throws IOException {
        Resource resource = new FileSystemResource(file);

        // 读取配置
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(100) // 忽略顶部100个单位的页眉
                .withPageBottomMargin(100) // 忽略底部100个单位的页脚
                .withPagesPerDocument(1) // 每个文档的页数
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfTopTextLinesToDelete(1)
                        .build()) // 每页再额外删除的行数
                .build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource, config);
        return reader.read();
    }
}
