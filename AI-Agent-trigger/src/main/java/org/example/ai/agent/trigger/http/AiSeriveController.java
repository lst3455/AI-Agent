package org.example.ai.agent.trigger.http;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.agent.domain.auth.service.IAuthService;
import org.example.ai.agent.domain.openai.model.aggregates.ChatProcessAggregate;
import org.example.ai.agent.domain.openai.service.IChatService;
import org.example.ai.agent.domain.utils.Utils;
import org.example.ai.agent.trigger.http.dto.ChatRagRequestDTO;
import org.example.ai.agent.trigger.http.dto.MessageEntity;
import org.example.ai.agent.types.common.Constants;
import org.example.ai.agent.types.exception.AiServiceException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Controller for handling AI service requests.
 * This controller provides endpoints for interacting with an LLM,
 * primarily focusing on generating streaming chat responses.
 */
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/agent/ai")
@Slf4j
public class AiSeriveController {

    @Resource
    private IAuthService authService;

    @Resource
    private IChatService chatService;

    /**
     * Endpoint for generating streaming RAG (Retrieval-Augmented Generation) responses.
     * Example: POST http://localhost:8090/api/v0/agent/ai/generate_stream_rag
     *
     * @param request  ChatRagRequestDTO containing chat messages and model info
     * @param token    Authorization token
     * @param response HttpServletResponse for streaming output
     * @return Flux<String> streaming the generated response
     */
    @RequestMapping(value = "generate_stream_rag", method = RequestMethod.POST)
    public Flux<String> generateStreamRag(@RequestBody ChatRagRequestDTO request,
                                          @RequestHeader("Authorization") String token,
                                          HttpServletResponse response) {
        log.info("Trigger RAG general generate, request:{}", JSON.toJSONString(request));
        try {
            // Step 1: Configure basic HTTP response properties for streaming.
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // Step 2: Perform token verification.
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
            List<MessageEntity> messageEntities = request.getMessages();
            // Avoid empty messageEntities
            if (messageEntities.isEmpty()) {
                messageEntities.add(MessageEntity.builder()
                        .role("user")
                        .content("Hi")
                        .build());
            }

            List<Message> aiMessages = messageEntities.stream()
                    .map(msg -> {
                        switch (msg.getRole().toLowerCase()) {
                            case "user":
                                return new UserMessage(msg.getContent());
                            case "system":
                                return new SystemMessage(msg.getContent());
                            case "assistant":
                                return new AssistantMessage(msg.getContent());
                            default:
                                log.warn("Unknown message role '{}', defaulting to user message.", msg.getRole());
                                return new UserMessage(msg.getContent());
                        }
                    })
                    .collect(Collectors.toList());

            // Step 5: Build the ChatProcessAggregate with necessary parameters for the chat service.
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .ragTag(request.getRagTag())
                    .build();

            // Step 6: Initiate the streaming chat generation through the chat service.
            return chatService.generateStreamRag(chatProcessAggregate);
        } catch (AiServiceException e) {
            log.error("Ai servie error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getCode(), e.getMessage()));
        } catch (ExecutionException e) {
            log.error("Cache access error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getMessage()));
        } catch (Exception e) {
            log.error("RAG general generate failed, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }

    /**
     * Endpoint for generating a title using the chat model.
     * Example: POST http://localhost:8090/api/v0/agent/ai/generate_title
     *
     * @param request  ChatRagRequestDTO containing chat messages and model info
     * @param token    Authorization token
     * @param response HttpServletResponse for streaming output
     * @return Flux<String> streaming the generated title
     */
    @RequestMapping(value = "generate_title", method = RequestMethod.POST)
    public Flux<String> generateTitle(@RequestBody ChatRagRequestDTO request,
                                      @RequestHeader("Authorization") String token,
                                      HttpServletResponse response) {
        log.info("Trigger generate title, request:{}", JSON.toJSONString(request));

        try {
            // 1. Basic configuration: stream output, encoding, disable caching
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. Token verification
            boolean success = authService.checkToken(token);
            if (!success) {
                log.info("Token verification failed");
                return Flux.just(Constants.ResponseCode.TOKEN_ERROR.getCode());
            }

            log.info("Token verification succeeded");

            // 3. Get OpenID
            String openid = authService.openid(token);
            log.info("Processing streaming title request, openid: {} Request model: {}", openid, request.getModel());

            List<MessageEntity> messageEntities = request.getMessages() != null ? request.getMessages() : new ArrayList<>();
            // Avoid empty messageEntities
            if (messageEntities.isEmpty()) {
                messageEntities.add(MessageEntity.builder()
                        .role("user")
                        .content("Hi")
                        .build());
            }

            // 4. Convert DTO messages to Spring AI messages
            List<Message> aiMessages = messageEntities.stream()
                    .map(msg -> {
                        switch (msg.getRole()) {
                            case "user":
                                return new UserMessage(msg.getContent());
                            case "system":
                                return new SystemMessage(msg.getContent());
                            case "assistant":
                                return new AssistantMessage(msg.getContent());
                            default:
                                return new UserMessage(msg.getContent());
                        }
                    })
                    .collect(Collectors.toList());

            // 5. Build parameters
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(aiMessages)
                    .build();

            // 6. Stream the response - extract just the text content from each ChatResponse
            return chatService.generateTitle(chatProcessAggregate);
        } catch (AiServiceException e) {
            log.error("Ai servie error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getCode(), e.getMessage()));
        } catch (ExecutionException e) {
            log.error("Cache access error, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Generate title failed, request: {} encountered an exception", request, e);
            return Flux.just(Utils.formatSseMessage("error", Constants.ResponseCode.UN_ERROR.getCode(),
                    Constants.ResponseCode.UN_ERROR.getInfo()));
        }
    }

}