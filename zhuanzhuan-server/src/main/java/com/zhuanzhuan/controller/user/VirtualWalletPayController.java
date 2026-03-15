package com.zhuanzhuan.controller.user;

import com.zhuanzhuan.dto.VirtualWalletCallbackDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.VirtualWalletPayService;
import com.zhuanzhuan.vo.PayStatusVO;
import com.zhuanzhuan.vo.VirtualWalletLaunchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VirtualWalletPayController {

    @Autowired
    private VirtualWalletPayService virtualWalletPayService;

    @PostMapping("/user/pay/virtual-wallet/{orderId}")
    public Result<VirtualWalletLaunchVO> launchVirtualWallet(@PathVariable Long orderId) {
        return Result.success(virtualWalletPayService.createPay(orderId));
    }

    @GetMapping("/user/pay/status/{orderId}")
    public Result<PayStatusVO> queryStatus(@PathVariable Long orderId) {
        return Result.success(virtualWalletPayService.queryPayStatus(orderId));
    }

    @PostMapping("/wallet/pay/callback")
    public Result<String> callback(@RequestBody VirtualWalletCallbackDTO dto) {
        virtualWalletPayService.handleCallback(dto);
        return Result.success("callback success");
    }
}
