package org.example.ai.agent.trigger.http;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.example.ai.agent.domain.auth.service.IAuthService;
import org.example.ai.agent.domain.openai.service.IRagService;
import org.example.ai.agent.trigger.http.dto.GeneralEmptyResponseDTO;
import org.example.ai.agent.trigger.http.dto.GitRepoUploadRequestDTO;
import org.example.ai.agent.trigger.http.dto.QueryRagTagsResponseDTO;
import org.example.ai.agent.types.common.Constants;
import org.example.ai.agent.types.exception.RagServiceException;
import org.example.ai.agent.types.model.Response;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/agent/rag")
@Slf4j
public class RagServiceController {

    @Resource
    private IAuthService authService;
    @Resource
    private IRagService iRagService;

    /**
     * Retrieves a list of available RAG (Retrieval Augmented Generation) tags.
     * Example URL: http://localhost:8092/api/v0/agent/rag/query_rag_tag_list
     *
     * @return A Response object containing a list of RAG tags.
     */
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.POST)
    public Response<QueryRagTagsResponseDTO> queryRagTagList(
            @RequestHeader("Authorization") String token,
            @RequestHeader("OpenId") String openId) {
        log.info("Query rag tag list, openId: {}", openId);
        try {
            // 1. Token validation
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<QueryRagTagsResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Parse token
            String openid = authService.openid(token);
            assert null != openid;

            List<String> elements = new ArrayList<>(iRagService.queryRagTags(openId));

            log.info("Query rag tag list completed, openId: {}", openId);
            return Response.<QueryRagTagsResponseDTO>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(QueryRagTagsResponseDTO.builder()
                            .ragTagList(new ArrayList<>(elements))
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Query rag tag list failed, openId: {}", openId, e);
            return Response.<QueryRagTagsResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Handles the uploading of files to the knowledge base, associating them with a specific RAG tag.
     * Example URL: http://localhost:8092/api/v0/agent/rag/file/upload
     *
     * @param ragTag The tag to associate with the uploaded knowledge documents.
     * @param files  A list of MultipartFile objects representing the files to be uploaded.
     * @return A Response object indicating the success or failure of the upload operation.
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public Response<GeneralEmptyResponseDTO> uploadFile(
            @RequestHeader("Authorization") String token,
            @RequestHeader("OpenId") String openId,
            @RequestParam("ragTag") String ragTag,
            @RequestParam("file") List<MultipartFile> files) {
        log.info("Context base upload started, openId: {}, RAG tag: {}", openId, ragTag);
        try {
            // 1. Token validation
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<GeneralEmptyResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Parse token
            String openid = authService.openid(token);
            assert null != openid;

            iRagService.fileUpload(openId, ragTag, files);

            log.info("Context base upload completed, openId: {}, RAG tag: {}", openId, ragTag);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (RagServiceException e) {
            log.error("Number of context reached limitation, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Context base upload failed, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Handles the uploading of Git repositories to the knowledge base, associating them with a specific RAG tag.
     * Example URL: http://localhost:8092/api/v0/agent/rag/gitRepo/publicRepo_upload
     *
     * @param token                   The authorization token for authentication
     * @param openId                  The user's unique identifier
     * @param gitRepoUploadRequestDTO DTO containing the RAG tag and Git repository URLs
     * @return A Response object indicating the success or failure of the upload operation
     */
    @RequestMapping(value = "gitRepo/publicRepo_upload", method = RequestMethod.POST)
    public Response<GeneralEmptyResponseDTO> uploadGitRepo(
            @RequestHeader("Authorization") String token,
            @RequestHeader("OpenId") String openId,
            @RequestBody GitRepoUploadRequestDTO gitRepoUploadRequestDTO) {

        String ragTag = gitRepoUploadRequestDTO.getRagTag();
        List<String> repoUrls = gitRepoUploadRequestDTO.getGitRepoUrls();
        log.info("Context base upload started, openId: {}, RAG tag: {}, RepoUrls: {}", openId, ragTag, repoUrls);
        try {
            // 1. Token validation
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<GeneralEmptyResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Parse token
            String openid = authService.openid(token);
            assert null != openid;

            iRagService.gitRepoUpload(openId, ragTag, repoUrls);

            log.info("Context base upload completed, openId: {}, RAG tag: {}, RepoUrls: {}", openId, ragTag, repoUrls);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (GitAPIException e) {
            log.error("Number of context reached limitation, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .info(e.getMessage())
                    .build();
        } catch (RagServiceException e) {
            log.error("Number of context reached limitation, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Context base upload failed, openId: {}, RAG tag: {}, RepoUrls: {}", openId, ragTag, repoUrls, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Handles deleting knowledge base, removing a specific RAG tag.
     * Example URL: http://localhost:8092/api/v0/agent/rag/delete_rag_context
     *
     * @param token  The authorization token for authentication
     * @param openId The user's unique identifier
     * @param ragTag The RAG tag to delete
     * @return A Response object indicating the success or failure of the delete operation
     */
    @RequestMapping(value = "delete_rag_context", method = RequestMethod.POST)
    public Response<GeneralEmptyResponseDTO> deleteRagContext(
            @RequestHeader("Authorization") String token,
            @RequestHeader("OpenId") String openId,
            @RequestParam("ragTag") String ragTag) {
        log.info("Context base delete started, openId: {}, RAG tag: {}", openId, ragTag);
        try {
            // 1. Token validation
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<GeneralEmptyResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Parse token
            String openid = authService.openid(token);
            assert null != openid;

            iRagService.deleteRagContext(openid, ragTag);

            log.info("Context base delete completed, openId: {}, RAG tag: {}", openId, ragTag);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("Context base delete failed, openId: {}, RAG tag: {}", openId, ragTag, e);
            return Response.<GeneralEmptyResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}