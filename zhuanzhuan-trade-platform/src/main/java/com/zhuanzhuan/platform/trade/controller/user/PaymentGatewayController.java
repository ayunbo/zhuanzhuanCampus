package com.zhuanzhuan.platform.trade.controller.user;

import com.zhuanzhuan.dto.PayCreateDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.trade.service.PaymentGatewayService;
import com.zhuanzhuan.vo.PayCreateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentGatewayController {

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @PostMapping("/user/pay/submit")
    public Result<PayCreateVO> submit(@RequestBody PayCreateDTO dto) {
        return Result.success(paymentGatewayService.submit(dto));
    }
}
