package org.example.ai.chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Current support Qwen3:1.7b, Qwen3:8b, Qwen3:14b
 */

@Configuration
public class ChatClientsConfig {

    /**
     * Local model using Ollama
     * @return
     */
    @Bean("qwen3_1_7bApi")
    @Primary
    public OllamaApi ollamaApiQwen3_1_7b() {
        return OllamaApi.builder()
                .baseUrl("http://117.72.127.104:11434")
                .build();
    }

    @Bean("qwen3_1_7bChatModel")
    public OllamaChatModel qwen3_1_7bChatModel(
            @Qualifier("qwen3_1_7bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:1.7b")
                                .build()
                )
                .build();
    }

    /**
     * Local model using Ollama
     * @return
     */
    @Bean("qwen3_8bApi")
    public OllamaApi ollamaApiQwen3_8b() {
        return OllamaApi.builder()
                .baseUrl("http://8.219.99.73:11434")
                .build();
    }

    @Bean("qwen3_8bChatModel")
    public OllamaChatModel qwen3_8bChatModel(
            @Qualifier("qwen3_8bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:8b")
                                .build()
                )
                .build();
    }

    /**
     * Local model using Ollama
     * @return
     */
    @Bean("qwen3_14bApi")
    public OllamaApi ollamaApiQwen3_14b() {
        return OllamaApi.builder()
                .baseUrl("http://8.219.99.73:11435")
                .build();
    }

    @Bean("qwen3_14bChatModel")
    public OllamaChatModel qwen3_14bChatModel(
            @Qualifier("qwen3_14bApi") OllamaApi api) {
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:14b")
                                .build()
                )
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("glm_4flashApi")
    public OpenAiApi openaiApiGlm_4flash(@Value("${ai.chatglm.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .apiKey(apiKey)
                .completionsPath("/chat/completions")
                .build();
    }

    @Bean("glm_4flashChatModel")
    public OpenAiChatModel glm_4flashChatModel(@Qualifier("glm_4flashApi") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4-flash")
                        .build())
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("qwen3_plusApi")
    public OpenAiApi openaiApiQwen3_plus(@Value("${ai.ertech.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://ai.erikpsw.works")
                .apiKey(apiKey)
                .build();
    }

    @Bean("qwen3_plusChatModel")
    public OpenAiChatModel qwen3_plusChatModel(@Qualifier("qwen3_plusApi") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-plus")
                        .build())
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("qwen3_235bApi")
    public OpenAiApi openaiApiQwen3_235b(@Value("${ai.ertech.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://ai.erikpsw.works")
                .apiKey(apiKey)
                .build();
    }

    @Bean("qwen3_235bChatModel")
    public OpenAiChatModel qwen3_235bChatModel(@Qualifier("qwen3_235bApi") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-235b-a22b")
                        .build())
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("qwen3_maxApi")
    public OpenAiApi openaiApiQwen3_max(@Value("${ai.ertech.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://ai.erikpsw.works")
                .apiKey(apiKey)
                .build();
    }

    @Bean("qwen3_maxChatModel")
    public OpenAiChatModel qwen3_maxChatModel(@Qualifier("qwen3_maxApi") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-max")
                        .build())
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("deepseek_r1Api")
    public OpenAiApi openaiApiDeepseek_r1(@Value("${ai.ertech.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://ai.erikpsw.works")
                .apiKey(apiKey)
                .build();
    }

    @Bean("deepseek_r1ChatModel")
    public OpenAiChatModel deepseek_r1ChatModel(@Qualifier("deepseek_r1Api") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("deepseek-r1")
                        .build())
                .build();
    }

    /**
     * Third party model using OpenAI format to call
     * @return
     */
    @Bean("deepseek_v3Api")
    public OpenAiApi openaiApiDeepseek_v3(@Value("${ai.ertech.apikey}") String apiKey) {
        // baseUrl should include /v1
        return OpenAiApi.builder()
                .baseUrl("https://ai.erikpsw.works")
                .apiKey(apiKey)
                .build();
    }

    @Bean("deepseek_v3ChatModel")
    public OpenAiChatModel deepseek_v3ChatModel(@Qualifier("deepseek_v3Api") OpenAiApi api) {
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("deepseek-v3")
                        .build())
                .build();
    }


    // 3) ChatClient beans for each model
    @Bean("chatClient_qwen3_1_7b")
    public ChatClient chatClientQwen3_7b(
            @Qualifier("qwen3_1_7bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_8b")
    public ChatClient chatClientQwen3_8b(
            @Qualifier("qwen3_8bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_14b")
    public ChatClient chatClientQwen3_14b(
            @Qualifier("qwen3_14bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_glm_4flash")
    public ChatClient chatClientGlm_4flash(@Qualifier("glm_4flashChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_235b")
    public ChatClient chatClientQwen3_235b(@Qualifier("qwen3_235bChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_plus")
    public ChatClient chatClientQwen3_plus(@Qualifier("qwen3_plusChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_qwen3_max")
    public ChatClient chatClientQwen3_max(@Qualifier("qwen3_maxChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_deepseek_r1")
    public ChatClient chatClientdeepseek_r1(@Qualifier("deepseek_r1ChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    @Bean("chatClient_deepseek_v3")
    public ChatClient chatClientdeepseek_v3(@Qualifier("deepseek_v3ChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }
}