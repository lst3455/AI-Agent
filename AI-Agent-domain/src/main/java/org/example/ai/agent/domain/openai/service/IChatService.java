package org.example.ai.agent.domain.openai.service;

import org.example.ai.agent.domain.openai.model.aggregates.ChatProcessAggregate;
import reactor.core.publisher.Flux;


public interface IChatService {

    Flux<String> generateStreamRag(ChatProcessAggregate chatProcessAggregate);

}
