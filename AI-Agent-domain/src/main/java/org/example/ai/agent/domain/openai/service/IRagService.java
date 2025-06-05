package org.example.ai.agent.domain.openai.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    public List<String> queryRagTags(String userId);

    public void fileUpload(String userId, String ragTag, List<MultipartFile> files);

    public void deleteRagContext(String userId, String ragTag);
}
