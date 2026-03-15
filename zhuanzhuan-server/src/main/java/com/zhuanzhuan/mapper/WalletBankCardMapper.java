package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.WalletBankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WalletBankCardMapper {

    List<WalletBankCard> listByWalletAccountId(Long walletAccountId);

    WalletBankCard getById(Long id);

    int deductBalance(@Param("id") Long id,
                      @Param("amount") BigDecimal amount);
}
