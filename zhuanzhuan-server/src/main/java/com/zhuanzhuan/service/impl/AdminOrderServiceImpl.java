package com.zhuanzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsStatusConstant;
import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.dto.AdminOrderStatusDTO;
import com.zhuanzhuan.dto.AdminOrderUpdateDTO;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.GoodsMapper;
import com.zhuanzhuan.mapper.OrderMapper;
import com.zhuanzhuan.mapper.OrderSnapshotMapper;
import com.zhuanzhuan.mapper.PayMapper;
import com.zhuanzhuan.mapper.PayRecordMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.AdminOrderService;
import com.zhuanzhuan.vo.AdminOrderDetailVO;
import com.zhuanzhuan.vo.OrderDetailVO;
import com.zhuanzhuan.vo.PayRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderSnapshotMapper orderSnapshotMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    public PageResult pageQuery(AdminOrderPageQueryDTO dto) {
        AdminOrderPageQueryDTO query = dto == null ? new AdminOrderPageQueryDTO() : dto;
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int pageSize = (query.getPageSize() == null || query.getPageSize() < 1) ? 10 : query.getPageSize();

        PageHelper.startPage(page, pageSize);
        Page<OrderDetailVO> pageInfo = (Page<OrderDetailVO>) orderMapper.adminPageQuery(query);
        return new PageResult(pageInfo.getTotal(), pageInfo.getResult());
    }

    @Override
    public AdminOrderDetailVO detail(Long id) {
        AdminOrderDetailVO vo = orderMapper.adminDetail(id);
        if (vo == null) {
            throw new BaseException("订单不存在");
        }

        List<PayRecordVO> payRecords = payRecordMapper.listByOrderId(id);
        vo.setPayRecords(payRecords);

        return vo;
    }

    @Override
    @Transactional
    public void updateOrder(AdminOrderUpdateDTO dto) {
        AdminOrderDetailVO detail = orderMapper.adminDetail(dto.getId());
        if (detail == null) {
            throw new BaseException("订单不存在");
        }

        if (dto.getMeetTime() != null && !dto.getMeetTime().isAfter(LocalDateTime.now())) {
            throw new BaseException("交易时间必须晚于当前时间");
        }

        orderSnapshotMapper.adminUpdateSnapshot(dto);
    }

    @Override
    @Transactional
    public void updateStatus(AdminOrderStatusDTO dto) {
        Order order = orderMapper.getById(dto.getId());
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        Integer newStatus = dto.getStatus();
        Integer oldStatus = order.getStatus();
        if (newStatus == null) {
            throw new BaseException("目标状态不能为空");
        }

        if (oldStatus.equals(newStatus)) {
            return;
        }

        // 统一入口：管理员修改订单状态后，同步订单时间字段、支付状态和商品状态
        orderMapper.adminUpdateStatus(order.getId(), newStatus);
        syncOrderTimeFields(order.getId(), newStatus);
        syncPayStatus(order.getId(), newStatus);
        syncGoodsStatus(order, newStatus);
        insertAdminStatusRecord(order.getId(), newStatus);
    }

    private void syncOrderTimeFields(Long orderId, Integer newStatus) {
        LocalDateTime now = LocalDateTime.now();

        if (OrderStatusConstant.PENDING_PAY.equals(newStatus)) {
            orderMapper.adminSetPayTime(orderId, null);
            orderMapper.adminSetCompleteTime(orderId, null);
            orderMapper.adminSetCloseTime(orderId, null);
            return;
        }

        if (OrderStatusConstant.PAID.equals(newStatus)) {
            orderMapper.adminSetPayTime(orderId, now);
            orderMapper.adminSetCompleteTime(orderId, null);
            orderMapper.adminSetCloseTime(orderId, null);
            return;
        }

        if (OrderStatusConstant.COMPLETED.equals(newStatus)) {
            orderMapper.adminSetPayTime(orderId, now);
            orderMapper.adminSetCompleteTime(orderId, now);
            orderMapper.adminSetCloseTime(orderId, null);
            return;
        }

        if (OrderStatusConstant.CANCELED.equals(newStatus)
                || OrderStatusConstant.TIMEOUT_CLOSED.equals(newStatus)) {
            orderMapper.adminSetCompleteTime(orderId, null);
            orderMapper.adminSetCloseTime(orderId, now);
        }
    }

    private void syncPayStatus(Long orderId, Integer newStatus) {
        if (OrderStatusConstant.PENDING_PAY.equals(newStatus)) {
            payMapper.adminUpdateStatus(orderId, PayStatusConstant.PENDING, null);
            return;
        }

        if (OrderStatusConstant.PAID.equals(newStatus)
                || OrderStatusConstant.COMPLETED.equals(newStatus)) {
            payMapper.adminUpdateStatus(orderId, PayStatusConstant.SUCCESS, LocalDateTime.now());
            return;
        }

        if (OrderStatusConstant.CANCELED.equals(newStatus)
                || OrderStatusConstant.TIMEOUT_CLOSED.equals(newStatus)) {
            payMapper.adminUpdateStatus(orderId, PayStatusConstant.CLOSED, null);
        }
    }

    private void syncGoodsStatus(Order order, Integer newStatus) {
        if (OrderStatusConstant.PENDING_PAY.equals(newStatus)
                || OrderStatusConstant.PAID.equals(newStatus)) {
            goodsMapper.adminSetStatus(order.getGoodsId(), GoodsStatusConstant.LOCKED, order.getId());
            return;
        }

        if (OrderStatusConstant.COMPLETED.equals(newStatus)) {
            goodsMapper.adminSetStatus(order.getGoodsId(), GoodsStatusConstant.SOLD, order.getId());
            return;
        }

        if (OrderStatusConstant.CANCELED.equals(newStatus)
                || OrderStatusConstant.TIMEOUT_CLOSED.equals(newStatus)) {
            goodsMapper.adminSetStatus(order.getGoodsId(), GoodsStatusConstant.ON_SALE, null);
        }
    }

    private void insertAdminStatusRecord(Long orderId, Integer newStatus) {
        Pay pay = payMapper.getByOrderId(orderId);
        if (pay == null) {
            return;
        }

        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(orderId);
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("管理员修改订单状态");
        payRecord.setStatus(mapPayRecordStatus(newStatus));
        payRecord.setChannelResponse("adminChangeOrderStatus=" + newStatus);
        payRecord.setCreateUser(0L);
        payRecord.setUpdateUser(0L);
        payRecordMapper.insert(payRecord);
    }

    private Integer mapPayRecordStatus(Integer orderStatus) {
        if (OrderStatusConstant.PENDING_PAY.equals(orderStatus)) {
            return PayStatusConstant.PENDING;
        }
        if (OrderStatusConstant.PAID.equals(orderStatus)
                || OrderStatusConstant.COMPLETED.equals(orderStatus)) {
            return PayStatusConstant.SUCCESS;
        }
        if (OrderStatusConstant.CANCELED.equals(orderStatus)
                || OrderStatusConstant.TIMEOUT_CLOSED.equals(orderStatus)) {
            return PayStatusConstant.CLOSED;
        }
        return PayStatusConstant.PENDING;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderMapper.getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        goodsMapper.unlockGoods(
                order.getGoodsId(),
                order.getId(),
                GoodsStatusConstant.LOCKED,
                GoodsStatusConstant.ON_SALE
        );

        payRecordMapper.deleteByOrderId(id);
        payMapper.deleteByOrderId(id);
        orderSnapshotMapper.deleteByOrderId(id);
        orderMapper.deleteById(id);
    }
}

