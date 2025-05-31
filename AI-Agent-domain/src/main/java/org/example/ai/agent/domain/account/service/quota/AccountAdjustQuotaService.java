package org.example.ai.agent.domain.account.service.quota;


import org.example.ai.agent.domain.account.adapter.repository.IAccountRepository;
import org.example.ai.agent.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.agent.domain.account.model.valobj.AccountQuotaVO;
import org.example.ai.agent.domain.account.service.IAccountAdjustQuotaService;
import org.springframework.stereotype.Service;


@Service
public class AccountAdjustQuotaService implements IAccountAdjustQuotaService {

    private final IAccountRepository repository;

    public AccountAdjustQuotaService(IAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity) {
        return repository.adjustQuota(adjustQuotaEntity);
    }

}
