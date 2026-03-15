package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.WalletAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface WalletAccountMapper {

    WalletAccount getByLoginName(String loginName);

    int deductBalance(@Param("id") Long id,
                      @Param("amount") BigDecimal amount);
}
