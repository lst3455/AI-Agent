package org.example.ai.agent.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRagRequestDTO {

    /** 默认模型 */
    private String model;

    /** 问题描述 */
    private List<MessageEntity> messages;

    /** rag tag */
    private String ragTag;

}
