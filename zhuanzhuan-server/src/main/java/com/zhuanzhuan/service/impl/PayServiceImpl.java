package com.zhuanzhuan.service.impl;

import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.dto.PaySubmitDTO;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.OrderMapper;
import com.zhuanzhuan.mapper.PayMapper;
import com.zhuanzhuan.mapper.PayRecordMapper;
import com.zhuanzhuan.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    @Transactional
    public void mockPay(PaySubmitDTO dto) {
        Long currentId = 1L;

        Order order = orderMapper.getById(dto.getOrderId());
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!currentId.equals(order.getBuyerId())) {
            throw new BaseException("无权支付该订单");
        }

        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            throw new BaseException("订单状态异常");
        }

        Pay pay = payMapper.getByOrderId(order.getId());
        if (pay == null) {
            throw new BaseException("支付单不存在");
        }

        if (!PayStatusConstant.PENDING.equals(pay.getStatus())) {
            throw new BaseException("支付状态异常");
        }

        LocalDateTime now = LocalDateTime.now();

        int payRows = payMapper.paySuccess(
                pay.getId(),
                PayStatusConstant.PENDING,
                PayStatusConstant.SUCCESS,
                now
        );
        if (payRows == 0) {
            throw new BaseException("支付状态异常");
        }

        int orderRows = orderMapper.paySuccess(
                order.getId(),
                OrderStatusConstant.PENDING_PAY,
                OrderStatusConstant.PAID,
                now
        );
        if (orderRows == 0) {
            throw new BaseException("订单状态异常");
        }

        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("模拟支付成功");
        payRecord.setStatus(PayStatusConstant.SUCCESS);
        payRecord.setChannelResponse("模拟支付完成");
        payRecord.setCreateUser(currentId);
        payRecord.setUpdateUser(currentId);

        payRecordMapper.insert(payRecord);
    }
}