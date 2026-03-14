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

        if (oldStatus.equals(newStatus)) {
            return;
        }

        // 1. 更新订单状态
        orderMapper.adminUpdateStatus(order.getId(), newStatus);

        // 2. 按目标状态联动处理
        if (OrderStatusConstant.CANCELED.equals(newStatus)
                || OrderStatusConstant.TIMEOUT_CLOSED.equals(newStatus)) {

            goodsMapper.unlockGoods(
                    order.getGoodsId(),
                    order.getId(),
                    GoodsStatusConstant.LOCKED,
                    GoodsStatusConstant.ON_SALE
            );

            payMapper.closePayByOrderId(
                    order.getId(),
                    PayStatusConstant.PENDING,
                    PayStatusConstant.CLOSED
            );

            Pay pay = payMapper.getByOrderId(order.getId());
            if (pay != null) {
                PayRecord payRecord = new PayRecord();
                payRecord.setPayId(pay.getId());
                payRecord.setOrderId(order.getId());
                payRecord.setRecordNo("PR" + System.currentTimeMillis());
                payRecord.setContent("管理员修改订单状态为关闭/取消");
                payRecord.setStatus(PayStatusConstant.CLOSED);
                payRecord.setChannelResponse("管理员后台操作");
                payRecord.setCreateUser(0L);
                payRecord.setUpdateUser(0L);
                payRecordMapper.insert(payRecord);
            }
        }

        if (OrderStatusConstant.PAID.equals(newStatus)) {
            orderMapper.adminSetPayTime(order.getId(), LocalDateTime.now());
            payMapper.adminPaySuccess(order.getId(), PayStatusConstant.SUCCESS, LocalDateTime.now());
        }

        if (OrderStatusConstant.COMPLETED.equals(newStatus)) {
            orderMapper.adminSetCompleteTime(order.getId(), LocalDateTime.now());

            goodsMapper.soldGoods(
                    order.getGoodsId(),
                    order.getId(),
                    GoodsStatusConstant.LOCKED,
                    GoodsStatusConstant.SOLD
            );
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderMapper.getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        // 如果商品当前还被这个订单锁定，先释放
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
