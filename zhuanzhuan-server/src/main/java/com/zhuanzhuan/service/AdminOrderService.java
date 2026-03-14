package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.dto.AdminOrderStatusDTO;
import com.zhuanzhuan.dto.AdminOrderUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AdminOrderDetailVO;

public interface AdminOrderService {

    PageResult pageQuery(AdminOrderPageQueryDTO dto);

    AdminOrderDetailVO detail(Long id);

    void updateOrder(AdminOrderUpdateDTO dto);

    void updateStatus(AdminOrderStatusDTO dto);

    void deleteOrder(Long id);
}