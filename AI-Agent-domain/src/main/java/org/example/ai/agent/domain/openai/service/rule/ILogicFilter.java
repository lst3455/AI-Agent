package org.example.ai.agent.domain.openai.service.rule;


import org.example.ai.agent.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.agent.domain.openai.model.entity.RuleLogicEntity;


public interface ILogicFilter<T> {

    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, T data) throws Exception;

}
