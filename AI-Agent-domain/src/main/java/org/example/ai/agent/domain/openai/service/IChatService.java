package org.example.ai.agent.domain.openai.service;

import org.example.ai.agent.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.agent.types.exception.AiServiceException;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutionException;


public interface IChatService {

    Flux<String> generateStreamRag(ChatProcessAggregate chatProcessAggregate) throws AiServiceException, ExecutionException;

    Flux<String> generateTitle(ChatProcessAggregate chatProcessAggregate) throws AiServiceException, ExecutionException;

}
