package com.zhuanzhuan.platform.trade.service;

import com.zhuanzhuan.dto.WalletAccountOpenDTO;
import com.zhuanzhuan.dto.WalletBankCardBindDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.WalletOverviewVO;

public interface UserWalletService {

    WalletOverviewVO overview();

    PageResult records(Integer page, Integer pageSize);

    void openAccount(WalletAccountOpenDTO dto);

    void bindBankCard(WalletBankCardBindDTO dto);

    void setDefaultBankCard(Long bankCardId);
}
