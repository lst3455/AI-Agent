package org.example.ai.agent.domain.account.service;


import org.example.ai.agent.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.agent.domain.account.model.valobj.AccountQuotaVO;


public interface IAccountAdjustQuotaService {

    /**
     * 调额接口
     *
     * @param adjustQuotaEntity 调额实体对象
     * @return 账户额度
     */
    AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity);

}
