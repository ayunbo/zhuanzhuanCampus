package com.zhuanzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsStatusConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.OrderPageQueryDTO;
import com.zhuanzhuan.dto.OrderSubmitDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.OrderSnapshot;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.GoodsMapper;
import com.zhuanzhuan.mapper.OrderMapper;
import com.zhuanzhuan.mapper.OrderSnapshotMapper;
import com.zhuanzhuan.mapper.PayMapper;
import com.zhuanzhuan.mapper.PayRecordMapper;
import com.zhuanzhuan.mapper.UserMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.OrderService;
import com.zhuanzhuan.vo.OrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderSnapshotMapper orderSnapshotMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    @Transactional
    public Long submit(OrderSubmitDTO dto) {
        // 正式联调后改回 BaseContext.getCurrentId()
        Long buyerId = 1L;

        Goods goods = goodsMapper.getById(dto.getGoodsId());
        if (goods == null) {
            throw new BaseException("商品不存在");
        }

        if (!GoodsStatusConstant.ON_SALE.equals(goods.getStatus())) {
            throw new BaseException("商品不在售，无法下单");
        }

        if (buyerId.equals(goods.getSellerId())) {
            throw new BaseException("不能购买自己的商品");
        }

        User buyer = userMapper.getById(buyerId);
        User seller = userMapper.getById(goods.getSellerId());

        // 1. 插入订单主表
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setGoodsId(goods.getId());
        order.setSellerId(goods.getSellerId());
        order.setBuyerId(buyerId);
        order.setAmount(goods.getPrice());
        order.setStatus(OrderStatusConstant.PENDING_PAY);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        order.setVersion(0);
        order.setCreateUser(buyerId);
        order.setUpdateUser(buyerId);

        orderMapper.insert(order);

        // 2. 插入订单快照表
        OrderSnapshot snapshot = new OrderSnapshot();
        snapshot.setOrderId(order.getId());
        snapshot.setGoodsTitle(goods.getTitle());
        snapshot.setGoodsCover(goods.getCover());
        snapshot.setGoodsPrice(goods.getPrice());
        snapshot.setSellerName(seller == null ? null : seller.getName());
        snapshot.setSellerPhone(seller == null ? null : seller.getPhone());
        snapshot.setBuyerName(buyer == null ? null : buyer.getName());
        snapshot.setBuyerPhone(buyer == null ? null : buyer.getPhone());
        snapshot.setMeetLocation(dto.getMeetLocation());
        snapshot.setMeetTime(dto.getMeetTime());
        snapshot.setRemark(dto.getRemark());
        snapshot.setCreateUser(buyerId);
        snapshot.setUpdateUser(buyerId);

        orderSnapshotMapper.insert(snapshot);

        // 3. 锁商品
        int rows = goodsMapper.lockGoods(
                goods.getId(),
                GoodsStatusConstant.ON_SALE,
                GoodsStatusConstant.LOCKED,
                order.getId()
        );
        if (rows == 0) {
            throw new BaseException("商品已被锁定，请勿重复购买");
        }

        // 4. 创建支付主表
        Pay pay = new Pay();
        pay.setPayNo("PAY" + System.currentTimeMillis());
        pay.setRequestNo("REQ" + System.currentTimeMillis());
        pay.setOrderId(order.getId());
        pay.setAmount(order.getAmount());
        pay.setMethod(1);
        pay.setStatus(PayStatusConstant.PENDING);
        pay.setVersion(0);
        pay.setCreateUser(buyerId);
        pay.setUpdateUser(buyerId);

        payMapper.insert(pay);

        // 5. 插入支付记录
        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("创建支付单");
        payRecord.setStatus(PayStatusConstant.PENDING);
        payRecord.setChannelResponse("模拟支付初始化");
        payRecord.setCreateUser(buyerId);
        payRecord.setUpdateUser(buyerId);

        payRecordMapper.insert(payRecord);

        return order.getId();
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Long buyerId = 1L;

        Order order = orderMapper.getByIdAndBuyerId(id, buyerId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            throw new BaseException("订单状态异常");
        }

        int rows = orderMapper.cancelOrder(
                id,
                buyerId,
                OrderStatusConstant.PENDING_PAY,
                OrderStatusConstant.CANCELED,
                LocalDateTime.now()
        );

        if (rows == 0) {
            throw new BaseException("订单状态异常");
        }

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
            payRecord.setContent("取消订单，关闭支付单");
            payRecord.setStatus(PayStatusConstant.CLOSED);
            payRecord.setChannelResponse("用户主动取消订单");
            payRecord.setCreateUser(buyerId);
            payRecord.setUpdateUser(buyerId);
            payRecordMapper.insert(payRecord);
        }
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Long buyerId = 1L;

        Order order = orderMapper.getByIdAndBuyerId(id, buyerId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!OrderStatusConstant.PAID.equals(order.getStatus())) {
            throw new BaseException("订单状态异常");
        }

        int rows = orderMapper.completeOrder(
                id,
                buyerId,
                OrderStatusConstant.PAID,
                OrderStatusConstant.COMPLETED,
                LocalDateTime.now()
        );

        if (rows == 0) {
            throw new BaseException("订单状态异常");
        }

        goodsMapper.soldGoods(
                order.getGoodsId(),
                order.getId(),
                GoodsStatusConstant.LOCKED,
                GoodsStatusConstant.SOLD
        );
    }

    @Override
    public PageResult pageQuery(OrderPageQueryDTO dto) {
        Long currentId = 1L;

        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<OrderDetailVO> page;

        if (dto.getType() != null && dto.getType() == 2) {
            page = (Page<OrderDetailVO>) orderMapper.pageQuerySell(dto, currentId);
        } else {
            page = (Page<OrderDetailVO>) orderMapper.pageQueryBuy(dto, currentId);
        }

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderDetailVO detail(Long id) {
        OrderDetailVO vo = orderMapper.getDetailById(id);
        if (vo == null) {
            throw new BaseException("订单不存在");
        }
        return vo;
    }
}