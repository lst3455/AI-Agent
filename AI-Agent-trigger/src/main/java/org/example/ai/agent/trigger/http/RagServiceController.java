package org.example.ai.agent.trigger.http;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.agent.domain.auth.service.IAuthService;
import org.example.ai.agent.types.common.Constants;
import org.example.ai.agent.types.exception.ChatGPTException;
import org.example.ai.agent.types.model.Response;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/agent/rag")
@Slf4j
public class RagServiceController {

    @Resource
    private IAuthService authService;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;

    /**
     * Retrieves a list of available RAG (Retrieval Augmented Generation) tags.
     * Example URL: http://localhost:8090/api/v0/agent/rag/query_rag_tag_list
     *
     * @return A Response object containing a list of RAG tags.
     */
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.POST)
    public Response<List<String>> queryRagTagList(@RequestHeader("Authorization") String token, @RequestHeader("OpenId") String openId) {
        log.info("Query rag tag list, openId: {}", openId);
        try{
            // 1. Token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<String>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Token 解析
            String openid = authService.openid(token);
            assert null != openid;

            RList<String> elements = redissonClient.getList("ragTag");
            return Response.<List<String>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(new ArrayList<>(elements))
                    .build();
        }catch (Exception e){
            log.error("Query rag tag list fail, openId: {}", openId, e);
            return Response.<List<String>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Handles the uploading of files to the knowledge base, associating them with a specific RAG tag.
     * Example URL: http://localhost:8090/api/v0/agent/rag/file/upload
     *
     * @param ragTag The tag to associate with the uploaded knowledge documents.
     * @param files  A list of MultipartFile objects representing the files to be uploaded.
     * @return A Response object indicating the success or failure of the upload operation.
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public Response<String> uploadFile(@RequestHeader("Authorization") String token, @RequestHeader("OpenId") String openId, @RequestParam String ragTag, @RequestParam("file") List<MultipartFile> files) {
        log.info("context base upload started, openId: {}, RAG tag: {}", openId, ragTag);
        try{
            // 1. Token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Token 解析
            String openid = authService.openid(token);
            assert null != openid;

            for (MultipartFile file : files) {
                TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
                List<Document> documents = documentReader.get();
                List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                // Add the RAG tag as metadata to each document and its splits.
                documents.forEach(doc -> doc.getMetadata().put("context", ragTag));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("context", ragTag));

                pgVectorStore.accept(documentSplitterList);

                // Add the RAG tag to the list of known tags if it's not already present.
                RList<String> elements = redissonClient.getList("ragTag");
                if (!elements.contains(ragTag)) {
                    elements.add(ragTag);
                }
            }

            log.info("context base upload completed, openId: {}, RAG tag: {}", openId, ragTag);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        }catch (Exception e){
            log.error("context base upload fail, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}