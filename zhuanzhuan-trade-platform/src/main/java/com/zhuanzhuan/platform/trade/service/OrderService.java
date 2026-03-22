package com.zhuanzhuan.platform.trade.service;

import com.zhuanzhuan.dto.OrderPageQueryDTO;
import com.zhuanzhuan.dto.OrderSubmitDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.OrderDetailVO;

public interface OrderService {

    Long submit(OrderSubmitDTO dto);

    void cancel(Long id);

    void complete(Long id);

    PageResult pageQuery(OrderPageQueryDTO dto);

    OrderDetailVO detail(Long id);
}