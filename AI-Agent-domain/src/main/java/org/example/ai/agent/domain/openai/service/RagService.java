package org.example.ai.agent.domain.openai.service;

import jakarta.annotation.Resource;
import org.example.ai.agent.domain.openai.repository.IRagRepository;
import org.redisson.api.RList;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class RagService implements IRagService{

    @Resource
    private IRagRepository iRagRepository;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;

    @Override
    public RList<String> queryRagTags(String userId) {
        return iRagRepository.queryRagTags(userId);
    }

    @Override
    public void fileUpload(String userId, String ragTag, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // Add both userId and ragTag as metadata
            documents.forEach(doc -> {
                doc.getMetadata().put("context", ragTag);
                doc.getMetadata().put("userId", userId);
            });

            documentSplitterList.forEach(doc -> {
                doc.getMetadata().put("context", ragTag);
                doc.getMetadata().put("userId", userId);
            });

            pgVectorStore.accept(documentSplitterList);

            RList<String> elements = this.queryRagTags(userId);
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }
    }

    @Override
    public void deleteRagContext(String userId, String ragTag) {
        pgVectorStore.delete("context == '" + ragTag + "' AND userId == '" + userId + "'");
        RList<String> elements = this.queryRagTags(userId);
        elements.remove(ragTag);
    }
}
