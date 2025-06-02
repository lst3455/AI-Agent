package org.example.ai.agent.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;            // Import for SearchRequest :contentReference[oaicite:9]{index=9}
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RagTest {

    @Autowired
    @Qualifier("chatClient_glm_4flash")
    private ChatClient chatClientGlm_4flash;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private SimpleVectorStore simpleVectorStore;

    @Resource
    private PgVectorStore pgVectorStore;

    @Test
    public void upload() {
        TikaDocumentReader reader = new TikaDocumentReader("./data/file.text");
        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
        log.info("Splitter returned {} docs", documentSplitterList.size());
        documentSplitterList.forEach(doc ->
                log.info("Doc content length: {}, metadata: {}", doc.getText().length(), doc.getMetadata())
        );

        documents.forEach(doc -> doc.getMetadata().put("context", "test"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("context", "test"));

        pgVectorStore.accept(documentSplitterList);
        log.info("upload success");
    }

    @Test
    public void chat() {
        String message = "who is Zhang Jie";
        String SYSTEM_PROMPT = """
                You MUST and ONLY can use the information from the GIVEN CONTEXTS section to answer the user's question. If the answer is not found in the GIVEN CONTEXTS, state that you do not have that information.
                GIVEN CONTEXTS:
                    {documents}
                """;

        SearchRequest request = SearchRequest.builder()
                .query(message)                                 // Set the user query :contentReference[oaicite:10]{index=10}
                .topK(5)                                        // Return top 5 similar documents :contentReference[oaicite:11]{index=11}
                .filterExpression("context == 'test'")  // Apply metadata filter :contentReference[oaicite:12]{index=12}
                .build();                                       // Build the SearchRequest object :contentReference[oaicite:13]{index=13}

        List<Document> documents = pgVectorStore.similaritySearch(request);

        String documentCollectors = documents.stream()
                .map(Document::getText)   // Use getText() instead of getContent()
                .collect(Collectors.joining());

        log.info("Retrieved documents for prompt: {}", documentCollectors);

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT)
                .createMessage(Map.of("documents", documentCollectors));

        List<Message> messages = new ArrayList<>();
        messages.add(ragMessage);
        messages.add(new UserMessage(message));

        log.info("rag enhanced message: {}", messages);

        ChatResponse chatResponse = chatClientGlm_4flash
                .prompt(new Prompt(messages))  // prompt(...) returns ChatClientRequestSpec
                .call()                        // Execute the request
                .chatResponse();               // Retrieve ChatResponse

        log.info("test result: {}", chatResponse.getResult());
    }
}
