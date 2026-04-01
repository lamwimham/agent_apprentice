package com.keepin.rag.embedding;

import kotlin.collections.ArrayDeque;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {
    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private PgVectorStore vectorStore;

    /**
     * Embed a list of documents
     * @param documentList
     * @return
     */
    public List<float[]> embed(List<Document> documentList) {
        return documentList.stream().map(document -> embeddingModel.embed(document.getText())).collect(Collectors.toList());
    }

    /**
     * 存储数据库
     * 向量化+保存数据库
     */
    public void embeddingAndSave(List<Document> documentList) {

        if (CollectionUtils.isEmpty(documentList)) {
            return;
        }
        List<List<Document>> batches = new ArrayList<>();

        for (int i = 0; i < documentList.size(); i += 9) {
            int end = Math.min(i + 9, documentList.size());
            batches.add(documentList.subList(i, end));
        }
        for (List<Document> batch : batches) {
            vectorStore.add(batch);
        }
    }

}
