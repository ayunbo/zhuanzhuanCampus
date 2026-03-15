package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.WalletPayConfirmDTO;
import com.zhuanzhuan.vo.WalletInfoVO;

public interface WalletPayService {

    WalletInfoVO getWalletInfo(Long orderId, String loginName);

    void confirmPay(WalletPayConfirmDTO dto);
}
