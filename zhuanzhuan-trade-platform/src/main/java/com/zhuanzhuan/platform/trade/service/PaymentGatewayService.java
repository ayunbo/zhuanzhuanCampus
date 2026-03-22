package com.zhuanzhuan.platform.trade.service;

import com.zhuanzhuan.dto.PayCreateDTO;
import com.zhuanzhuan.vo.PayCreateVO;

public interface PaymentGatewayService {

    PayCreateVO submit(PayCreateDTO dto);
}
