package com.keepin.rag.reader;

import org.springframework.ai.document.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IDocumentReaderStrategy {

    /**
     * 是否支持读取文档
     */
    boolean supports(File file);

    /**
     * 读取指定文档，返回DocumentList
     */
    List<Document> read(File file) throws IOException;

}
