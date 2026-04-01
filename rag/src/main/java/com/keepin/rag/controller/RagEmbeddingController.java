package com.keepin.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.keepin.rag.embedding.EmbeddingService;
import com.keepin.rag.reader.DocumentStrategyFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag/embedding")
public class RagEmbeddingController {


    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private final DocumentStrategyFactory documentStrategyFactory;

    public RagEmbeddingController(DocumentStrategyFactory documentStrategyFactory) {
        this.documentStrategyFactory = documentStrategyFactory;
    }

    @RequestMapping("/test")
    public String test() {
        for(float f : embeddingModel.embed("test")) {
            System.out.println(f);
        }
        System.out.println(embeddingModel.embed("test").length);
        return "success" ;
    }


    @RequestMapping("/embed")
    public String embed(String filePath) {
        List<Document> documentList;

        try {
            documentList = documentStrategyFactory.read(new File(filePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Document> allChunkDocuments = documentList.stream()
                .flatMap(document ->
                        {
                            RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(300, new String[] {"\n", "\n\n"});
                            return splitter.split(document).stream();
                        })
                .toList();

        allChunkDocuments = allChunkDocuments.subList(0, 14);
        embeddingService.embeddingAndSave(allChunkDocuments);
        return documentList.toString();
    }
}
