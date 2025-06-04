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
    public RList<String> queryRagTags() {
        return iRagRepository.queryRagTags();
    }

    @Override
    public void fileUpload(String ragTag, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // Add the RAG tag as metadata to each document and its splits.
            documents.forEach(doc -> doc.getMetadata().put("context", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("context", ragTag));

            pgVectorStore.accept(documentSplitterList);

            // Add the RAG tag to the list of known tags if it's not already present.
            RList<String> elements = this.queryRagTags();
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }
    }

    @Override
    public void deleteRagContext(String ragTag) {
        pgVectorStore.delete("context == '" + ragTag + "'");
        RList<String> elements = this.queryRagTags();
        elements.remove(ragTag);
    }
}
