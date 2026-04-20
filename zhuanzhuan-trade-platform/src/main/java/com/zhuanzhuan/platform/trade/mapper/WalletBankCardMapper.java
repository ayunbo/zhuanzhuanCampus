package com.zhuanzhuan.platform.trade.mapper;

import com.zhuanzhuan.entity.WalletBankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WalletBankCardMapper {

    List<WalletBankCard> listByWalletAccountId(Long walletAccountId);

    WalletBankCard getById(Long id);

    WalletBankCard getByCardNo(String cardNo);

    void insert(WalletBankCard walletBankCard);

    int clearDefaultByWalletAccountId(@Param("walletAccountId") Long walletAccountId,
                                      @Param("updateUser") Long updateUser);

    int setDefault(@Param("id") Long id,
                   @Param("walletAccountId") Long walletAccountId,
                   @Param("updateUser") Long updateUser);

    int deductBalance(@Param("id") Long id,
                      @Param("amount") BigDecimal amount);
}
