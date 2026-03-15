package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.VirtualWalletCallbackDTO;
import com.zhuanzhuan.vo.PayStatusVO;
import com.zhuanzhuan.vo.VirtualWalletLaunchVO;

public interface VirtualWalletPayService {

    VirtualWalletLaunchVO createPay(Long orderId);

    PayStatusVO queryPayStatus(Long orderId);

    void handleCallback(VirtualWalletCallbackDTO dto);
}
