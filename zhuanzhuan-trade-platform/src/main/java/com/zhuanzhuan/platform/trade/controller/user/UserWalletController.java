package com.zhuanzhuan.platform.trade.controller.user;

import com.zhuanzhuan.dto.WalletAccountOpenDTO;
import com.zhuanzhuan.dto.WalletBankCardBindDTO;
import com.zhuanzhuan.platform.trade.service.UserWalletService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.WalletOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserWalletController {

    @Autowired
    private UserWalletService userWalletService;

    @GetMapping("/user/wallet/overview")
    public Result<WalletOverviewVO> overview() {
        return Result.success(userWalletService.overview());
    }

    @GetMapping("/user/wallet/records")
    public Result<PageResult> records(@RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        return Result.success(userWalletService.records(page, pageSize));
    }

    @PostMapping("/user/wallet/open")
    public Result<String> openAccount(@RequestBody WalletAccountOpenDTO dto) {
        userWalletService.openAccount(dto);
        return Result.success("钱包开通成功");
    }

    @PostMapping("/user/wallet/bank-card")
    public Result<String> bindBankCard(@RequestBody WalletBankCardBindDTO dto) {
        userWalletService.bindBankCard(dto);
        return Result.success("银行卡绑定成功");
    }

    @PutMapping("/user/wallet/bank-card/default/{bankCardId}")
    public Result<String> setDefaultBankCard(@PathVariable Long bankCardId) {
        userWalletService.setDefaultBankCard(bankCardId);
        return Result.success("默认银行卡设置成功");
    }
}
