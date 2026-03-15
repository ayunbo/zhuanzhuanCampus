package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.PayCreateDTO;
import com.zhuanzhuan.vo.PayCreateVO;

public interface PaymentGatewayService {

    PayCreateVO submit(PayCreateDTO dto);
}
