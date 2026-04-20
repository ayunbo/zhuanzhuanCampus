package com.zhuanzhuan.platform.trade.service.impl;

import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayMethodConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.VirtualWalletCallbackDTO;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.trade.mapper.OrderMapper;
import com.zhuanzhuan.platform.trade.mapper.PayMapper;
import com.zhuanzhuan.platform.trade.mapper.PayRecordMapper;
import com.zhuanzhuan.properties.VirtualWalletProperties;
import com.zhuanzhuan.platform.trade.service.VirtualWalletPayService;
import com.zhuanzhuan.utils.VirtualWalletSignUtil;
import com.zhuanzhuan.vo.PayStatusVO;
import com.zhuanzhuan.vo.VirtualWalletLaunchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class VirtualWalletPayServiceImpl implements VirtualWalletPayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Autowired
    private VirtualWalletProperties virtualWalletProperties;

    @Override
    @Transactional
    public VirtualWalletLaunchVO createPay(Long orderId) {
        Long currentId = getCurrentUserId();
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!currentId.equals(order.getBuyerId())) {
            throw new BaseException("无权支付该订单");
        }
        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            throw new BaseException("当前订单不可支付");
        }
        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BaseException("订单已过期，请重新下单");
        }

        Pay pay = payMapper.getByOrderId(orderId);
        if (pay == null) {
            throw new BaseException("支付单不存在");
        }
        if (!PayStatusConstant.PENDING.equals(pay.getStatus())) {
            throw new BaseException("当前支付单不可再次发起");
        }

        String requestNo = buildRequestNo(orderId);
        int rows = payMapper.updateLaunchInfo(pay.getId(), requestNo, PayMethodConstant.VIRTUAL_WALLET, currentId);
        if (rows == 0) {
            throw new BaseException("支付单状态已变化，请刷新后重试");
        }

        pay.setRequestNo(requestNo);
        pay.setMethod(PayMethodConstant.VIRTUAL_WALLET);

        VirtualWalletLaunchVO vo = new VirtualWalletLaunchVO();
        vo.setOrderId(order.getId());
        vo.setPayId(pay.getId());
        vo.setPayNo(pay.getPayNo());
        vo.setRequestNo(requestNo);
        vo.setAmount(pay.getAmount());
        vo.setMethod(pay.getMethod());
        vo.setPayStatus(pay.getStatus());
        vo.setExpireTime(order.getExpireTime());
        vo.setCallbackUrl(virtualWalletProperties.getCallbackUrl());
        vo.setReturnUrl(virtualWalletProperties.getReturnUrl());
        vo.setWalletScheme(buildWalletScheme(order, pay, requestNo));
        return vo;
    }

    @Override
    public PayStatusVO queryPayStatus(Long orderId) {
        Long currentId = getCurrentUserId();
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!currentId.equals(order.getBuyerId()) && !currentId.equals(order.getSellerId())) {
            throw new BaseException("无权查看支付状态");
        }

        Pay pay = payMapper.getByOrderId(orderId);
        if (pay == null) {
            throw new BaseException("支付单不存在");
        }

        PayStatusVO vo = new PayStatusVO();
        vo.setOrderId(order.getId());
        vo.setPayId(pay.getId());
        vo.setPayNo(pay.getPayNo());
        vo.setRequestNo(pay.getRequestNo());
        vo.setMethod(pay.getMethod());
        vo.setPayStatus(pay.getStatus());
        vo.setOrderStatus(order.getStatus());
        vo.setExpireTime(order.getExpireTime());
        vo.setPayTime(pay.getPayTime());
        return vo;
    }

    @Override
    @Transactional
    public void handleCallback(VirtualWalletCallbackDTO dto) {
        validateCallback(dto);

        Pay pay = payMapper.getByRequestNo(dto.getRequestNo());
        if (pay == null) {
            throw new BaseException("支付请求不存在");
        }

        Order order = orderMapper.getById(pay.getOrderId());
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!Objects.equals(order.getId(), dto.getOrderId())) {
            throw new BaseException("订单与支付请求不匹配");
        }
        if (pay.getAmount() == null || pay.getAmount().compareTo(dto.getAmount()) != 0) {
            throw new BaseException("支付金额不匹配");
        }
        if (!PayStatusConstant.PENDING.equals(pay.getStatus())) {
            return;
        }
        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            return;
        }

        if (PayStatusConstant.SUCCESS.equals(dto.getPayStatus())) {
            handleSuccess(order, pay, dto);
            return;
        }

        insertFailRecord(order, pay, dto);
    }

    private void handleSuccess(Order order, Pay pay, VirtualWalletCallbackDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        int payRows = payMapper.paySuccess(pay.getId(), PayStatusConstant.PENDING, PayStatusConstant.SUCCESS, now);
        if (payRows == 0) {
            return;
        }

        int orderRows = orderMapper.paySuccess(order.getId(), OrderStatusConstant.PENDING_PAY, OrderStatusConstant.PAID, now);
        if (orderRows == 0) {
            throw new BaseException("订单状态异常");
        }

        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("虚拟钱包支付成功");
        payRecord.setStatus(PayStatusConstant.SUCCESS);
        payRecord.setChannelResponse(buildChannelResponse(dto));
        payRecord.setCreateUser(order.getBuyerId());
        payRecord.setUpdateUser(order.getBuyerId());
        payRecordMapper.insert(payRecord);
    }

    private void insertFailRecord(Order order, Pay pay, VirtualWalletCallbackDTO dto) {
        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("虚拟钱包支付失败");
        payRecord.setStatus(PayStatusConstant.FAIL);
        payRecord.setChannelResponse(buildChannelResponse(dto));
        payRecord.setCreateUser(order.getBuyerId());
        payRecord.setUpdateUser(order.getBuyerId());
        payRecordMapper.insert(payRecord);
    }

    private void validateCallback(VirtualWalletCallbackDTO dto) {
        if (dto == null) {
            throw new BaseException("回调参数不能为空");
        }
        if (dto.getOrderId() == null || dto.getAmount() == null || dto.getPayStatus() == null) {
            throw new BaseException("支付回调参数不完整");
        }
        if (dto.getTimestamp() == null || dto.getRequestNo() == null || dto.getRequestNo().isBlank()) {
            throw new BaseException("支付回调参数不完整");
        }
        if (!PayStatusConstant.SUCCESS.equals(dto.getPayStatus()) && !PayStatusConstant.FAIL.equals(dto.getPayStatus())) {
            throw new BaseException("未知的支付结果状态");
        }
        if (!VirtualWalletSignUtil.verify(dto, virtualWalletProperties.getSignSecret())) {
            throw new BaseException("虚拟钱包签名校验失败");
        }
    }

    private String buildWalletScheme(Order order, Pay pay, String requestNo) {
        return UriComponentsBuilder.fromUriString(virtualWalletProperties.getScheme())
                .queryParam("appId", virtualWalletProperties.getAppId())
                .queryParam("requestNo", requestNo)
                .queryParam("orderId", order.getId())
                .queryParam("orderNo", order.getOrderNo())
                .queryParam("amount", normalizeAmount(pay.getAmount()))
                .queryParam("callbackUrl", virtualWalletProperties.getCallbackUrl())
                .queryParam("returnUrl", virtualWalletProperties.getReturnUrl())
                .build(true)
                .toUriString();
    }

    private String buildRequestNo(Long orderId) {
        return "VW" + System.currentTimeMillis() + orderId;
    }

    private String buildChannelResponse(VirtualWalletCallbackDTO dto) {
        return "transactionNo=" + safe(dto.getTransactionNo()) +
                ", walletUserId=" + safe(dto.getWalletUserId()) +
                ", walletAccount=" + safe(dto.getWalletAccount()) +
                ", bankCardMask=" + safe(dto.getBankCardMask()) +
                ", message=" + safe(dto.getMessage());
    }

    private Long getCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("用户未登录");
        }
        return currentId;
    }

    private String normalizeAmount(BigDecimal amount) {
        return amount == null ? "" : amount.stripTrailingZeros().toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
