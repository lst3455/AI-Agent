package org.example.ai.agent.domain.openai.repository;

import org.redisson.api.RList;

public interface IRagRepository {
    public RList<String> queryRagTags();
}
