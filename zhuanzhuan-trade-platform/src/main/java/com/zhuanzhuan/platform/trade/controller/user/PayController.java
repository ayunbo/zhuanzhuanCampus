package com.zhuanzhuan.platform.trade.controller.user;

import com.zhuanzhuan.dto.PaySubmitDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.trade.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @PostMapping("/mock")
    public Result<String> mockPay(@RequestBody PaySubmitDTO dto) {
        payService.mockPay(dto);
        return Result.success("支付成功");
    }
}