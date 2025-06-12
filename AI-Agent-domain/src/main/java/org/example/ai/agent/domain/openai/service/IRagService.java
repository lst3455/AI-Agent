package org.example.ai.agent.domain.openai.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IRagService {

    public List<String> queryRagTags(String userId);

    public void fileUpload(String userId, String ragTag, List<MultipartFile> files);

    public void gitRepoUpload(String userId, String ragTag, List<String> repoUrls) throws IOException, GitAPIException;

    public void deleteRagContext(String userId, String ragTag);
}
