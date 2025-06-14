package org.example.ai.agent.domain.openai.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.example.ai.agent.domain.openai.repository.IRagRepository;
import org.example.ai.agent.types.common.Constants;
import org.example.ai.agent.types.exception.RagServiceException;
import org.redisson.api.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;

@Service
@Slf4j
public class RagService implements IRagService {

    @Resource
    private IRagRepository iRagRepository;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;

    private static final long MAX_FILE_SIZE = 3L * 1024 * 1024;
    private static final long MAX_GIT_RESOURCE_SIZE = 300L * 1024;

    @Override
    public RList<String> queryRagTags(String userId) {
        return iRagRepository.queryRagTags(userId);
    }

    @Override
    public void fileUpload(String userId, String ragTag, List<MultipartFile> files) {
        // user can upload up to 5 context
        RList<String> elements = this.queryRagTags(userId);
        if (elements.size() == 5) {
            throw new RagServiceException(Constants.ResponseCode.REACH_UPLOAD_LIMIT.getCode(), Constants.ResponseCode.REACH_UPLOAD_LIMIT.getInfo());
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds 3 MB");
            }

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

           // Do upsert operation
            pgVectorStore.delete("context == '" + ragTag + "' AND userId == '" + userId + "'" + " AND source == '" + file.getOriginalFilename() + "'");
            pgVectorStore.accept(documentSplitterList);

            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }
    }

    @Override
    public void gitRepoUpload(String userId, String ragTag, List<String> repoUrls) throws IOException, GitAPIException {
        RList<String> elements = this.queryRagTags(userId);
        if (elements.size() == 5) {
            throw new RagServiceException(Constants.ResponseCode.REACH_UPLOAD_LIMIT.getCode(), Constants.ResponseCode.REACH_UPLOAD_LIMIT.getInfo());
        }

        String localPath = "./git-cloned-repo";

        for (String repoUrl : repoUrls) {
            FileUtils.deleteDirectory(new File(localPath));
            Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userId, token))
                    .call();

            // Use Files.walkFileTree to traverse directories
            Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.size(file) > MAX_GIT_RESOURCE_SIZE) {
                        log.warn("File " + file.getFileName() + " exceeds 50 KB, will be skip");
                        return FileVisitResult.CONTINUE;
                    }

                    TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                    List<Document> documents = reader.get();
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
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
            git.close();
        }

        FileUtils.deleteDirectory(new File(localPath));

        if (!elements.contains(ragTag)) {
            elements.add(ragTag);
        }
    }

    @Override
    public void deleteRagContext(String userId, String ragTag) {
        pgVectorStore.delete("context == '" + ragTag + "' AND userId == '" + userId + "'");
        RList<String> elements = this.queryRagTags(userId);
        elements.remove(ragTag);
    }
}
