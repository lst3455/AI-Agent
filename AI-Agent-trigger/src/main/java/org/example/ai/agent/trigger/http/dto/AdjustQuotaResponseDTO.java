package org.example.ai.agent.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustQuotaResponseDTO {

    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;

}
