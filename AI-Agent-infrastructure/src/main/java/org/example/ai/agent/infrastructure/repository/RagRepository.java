package org.example.ai.agent.infrastructure.repository;

import jakarta.annotation.Resource;
import org.example.ai.agent.domain.openai.repository.IRagRepository;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

@Repository
public class RagRepository implements IRagRepository {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public RList<String> queryRagTags() {
        return redissonClient.getList("ragTag");
    }
}
