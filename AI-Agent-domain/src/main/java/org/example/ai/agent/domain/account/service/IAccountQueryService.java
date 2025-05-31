package org.example.ai.agent.domain.account.service;


import org.example.ai.agent.domain.account.model.valobj.AccountQuotaVO;


public interface IAccountQueryService {

    AccountQuotaVO queryAccountQuota(String openid);

}
