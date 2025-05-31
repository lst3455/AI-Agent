package org.example.ai.agent.domain.openai.repository;


import org.example.ai.agent.domain.openai.model.entity.UserAccountEntity;

public interface IOpenAiRepository {

    int subAccountQuota(String openai);

    UserAccountEntity queryUserAccount(String openid);

    void insertUserAccount(String openid);
}
