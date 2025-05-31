package org.example.ai.agent.infrastructure.dao;


import org.apache.ibatis.annotations.Mapper;
import org.example.ai.agent.infrastructure.po.UserAccountPO;


@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);

    void insertUserAccount(UserAccountPO userAccountPO);

    int addAccountQuota(UserAccountPO userAccountPOReq);
}
