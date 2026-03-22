package com.zhuanzhuan.platform.account.controller.user;

import com.zhuanzhuan.dto.SellerAuthApplyDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.account.service.SellerAuthService;
import com.zhuanzhuan.vo.SellerAuthResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户卖家申请接口")
@RestController
@RequestMapping("/user/seller-auth")
public class UserSellerAuthController {

    @Autowired
    private SellerAuthService sellerAuthService;

    @Operation(summary = "提交卖家申请")
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody SellerAuthApplyDTO sellerAuthApplyDTO) {
        sellerAuthService.submitSellerAuth(sellerAuthApplyDTO);
        return Result.success();
    }

    @Operation(summary = "查询卖家申请结果")
    @GetMapping("/result")
    public Result<SellerAuthResultVO> getResult() {
        return Result.success(sellerAuthService.getCurrentSellerAuthResult());
    }

    @Operation(summary = "卖家专属接口示例")
    @GetMapping("/portal")
    public Result<Void> sellerPortal() {
        sellerAuthService.checkCurrentUserIsSeller();
        return Result.success();
    }
}
