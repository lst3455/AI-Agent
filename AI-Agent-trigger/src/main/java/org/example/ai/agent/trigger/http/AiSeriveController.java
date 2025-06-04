package org.example.ai.agent.trigger.http;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.agent.domain.auth.service.IAuthService;
import org.example.ai.agent.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.agent.domain.openai.service.IChatService;
import org.example.ai.agent.trigger.http.dto.ChatRagRequestDTO;
import org.example.ai.agent.trigger.http.dto.ChatRequestDTO;
import org.example.ai.agent.trigger.http.dto.MessageEntity;
import org.example.ai.agent.types.common.Constants;
import org.example.ai.agent.types.exception.ChatGPTException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling AI service requests.
 * This controller provides endpoints for interacting with an LLM,
 * primarily focusing on generating streaming chat responses.
 */
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/agent/ollama")
@Slf4j
public class AiSeriveController {

    @Resource
    private IAuthService authService;

    @Resource
    private IChatService chatService;

    @RequestMapping(value = "generate_stream_rag", method = RequestMethod.POST)
    public Flux<String> generateStreamRag(@RequestBody ChatRagRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("trigger rag generate, request:{}", JSON.toJSONString(request));
        try {
            // Step 1: Configure basic HTTP response properties for streaming.
            // Sets content type to text/event-stream, UTF-8 character encoding, and disables caching.
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // Step 2: Perform token verification.
            // Checks if the provided authorization token is valid.
            boolean success = authService.checkToken(token);
            if (!success) {
                log.warn("Token verification failed for token: {}", token);
                return Flux.just(Constants.ResponseCode.TOKEN_ERROR.getCode());
            }

            log.info("Token verification succeeded.");

            // Step 3: Retrieve the OpenID associated with the validated token.
            String openid = authService.openid(token);
            log.info("Processing streaming Q&A request for openid: {} with model: {}", openid, request.getModel());

            // Step 4: Convert incoming DTO messages to Spring AI Message objects.
            // This maps roles (user, system, assistant) to their respective Spring AI message types.
            List<MessageEntity> messageEntities = request.getMessages();
            // Avoid empty messageEntities
            if (messageEntities.isEmpty()) messageEntities.add(MessageEntity.builder()
                    .role("user")
                    .content("Hi")
                    .build());

            List<Message> aiMessages = request.getMessages().stream()
                    .map(msg -> {
                        switch (msg.getRole().toLowerCase()) { // Normalize role to lowercase for robust matching
                            case "user":
                                return new UserMessage(msg.getContent());
                            case "system":
                                return new SystemMessage(msg.getContent());
                            case "assistant":
                                return new AssistantMessage(msg.getContent());
                            default:
                                log.warn("Unknown message role '{}', defaulting to user message.", msg.getRole());
                                return new UserMessage(msg.getContent()); // Default to UserMessage for unknown roles
                        }
                    })
                    .collect(Collectors.toList());

            // Step 5: Build the ChatProcessAggregate with necessary parameters for the chat service.
            // This includes the user's OpenID, the requested model, and the converted list of messages.
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .ragTag(request.getRagTag())
                    .build();

            // Step 6: Initiate the streaming chat generation through the chat service.
            // The service will return a Flux<String> emitting parts of the response as they are generated.
            return chatService.generateStreamRag(chatProcessAggregate);
        } catch (Exception e) {
            log.error("Rag Streaming response, request: {} encountered an exception", request, e);
            throw new ChatGPTException(e.getMessage());
        }
    }
}