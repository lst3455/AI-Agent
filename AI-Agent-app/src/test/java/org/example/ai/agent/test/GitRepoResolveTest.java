package org.example.ai.agent.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GitRepoResolveTest {

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void analyzeGitRepository() throws IOException, GitAPIException {
        String repoUrl = "https://github.com/lst3455/AI-Agent";

        String localPath = "./git-cloned-repo";
        String repoProjectName = extractProjectName(repoUrl);
        log.info("Clone path: {}", new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(localPath))
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                .call();

        // Use Files.walkFileTree to traverse directories
        Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                log.info("{} Traversing path, uploading to knowledge base: {}", repoProjectName, file.getFileName());
                try {
                    TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                    List<Document> documents = reader.get();
                    List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                    // Add both userId and ragTag as metadata
                    documents.forEach(doc -> {
                        doc.getMetadata().put("context", repoProjectName);
                        doc.getMetadata().put("userId", "test0001");
                    });

                    documentSplitterList.forEach(doc -> {
                        doc.getMetadata().put("context", repoProjectName);
                        doc.getMetadata().put("userId", "test0001");
                    });

                    pgVectorStore.accept(documentSplitterList);
                } catch (Exception e) {
                    log.error("Failed to parse path and upload to knowledge base: {}", file.getFileName());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.info("Failed to access file: {} - {}", file.toString(), exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        git.close();
        FileUtils.deleteDirectory(new File(localPath));

        // Add knowledge base record
        RList<String> elements = redissonClient.getList("ragTag_test0001");
        if (!elements.contains(repoProjectName)) {
            elements.add(repoProjectName);
        }

        log.info("Path traversal and upload completed: {}", repoUrl);
    }

    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
    }
}