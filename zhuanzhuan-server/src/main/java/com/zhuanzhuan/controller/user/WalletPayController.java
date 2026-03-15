package com.zhuanzhuan.controller.user;

import com.zhuanzhuan.dto.WalletPayConfirmDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.WalletPayService;
import com.zhuanzhuan.vo.WalletInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletPayController {

    @Autowired
    private WalletPayService walletPayService;

    @GetMapping("/wallet/page/info")
    public Result<WalletInfoVO> getWalletInfo(@RequestParam Long orderId,
                                              @RequestParam String loginName) {
        return Result.success(walletPayService.getWalletInfo(orderId, loginName));
    }

    @PostMapping("/wallet/page/confirm")
    public Result<String> confirmPay(@RequestBody WalletPayConfirmDTO dto) {
        walletPayService.confirmPay(dto);
        return Result.success("支付成功");
    }
}
