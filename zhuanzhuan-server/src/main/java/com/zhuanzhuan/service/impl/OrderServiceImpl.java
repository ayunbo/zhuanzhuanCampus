package com.zhuanzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsStatusConstant;
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

    /**
     * 获取当前登录用户id
     */
    private Long getCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException("用户未登录");
        }
        return currentId;
    }

    /**
     * 买家提交订单
     */
    @Override
    @Transactional
    public Long submit(OrderSubmitDTO dto) {
        Long buyerId = getCurrentUserId();

        // 交易时间后端兜底校验
        if (dto.getMeetTime() != null && !dto.getMeetTime().isAfter(LocalDateTime.now())) {
            throw new BaseException("交易时间必须晚于当前时间");
        }

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

        // 3. 锁定商品，防止重复下单
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

    /**
     * 取消未支付订单
     */
    @Override
    @Transactional
    public void cancel(Long id) {
        Long buyerId = getCurrentUserId();

        Order order = orderMapper.getByIdAndBuyerId(id, buyerId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            throw new BaseException("当前订单不可取消");
        }

        int rows = orderMapper.cancelOrder(
                id,
                buyerId,
                OrderStatusConstant.PENDING_PAY,
                OrderStatusConstant.CANCELED,
                LocalDateTime.now()
        );

        if (rows == 0) {
            throw new BaseException("取消失败，订单状态异常");
        }

        // 释放商品锁定
        goodsMapper.unlockGoods(
                order.getGoodsId(),
                order.getId(),
                GoodsStatusConstant.LOCKED,
                GoodsStatusConstant.ON_SALE
        );

        // 关闭支付单
        payMapper.closePayByOrderId(
                order.getId(),
                PayStatusConstant.PENDING,
                PayStatusConstant.CLOSED
        );

        // 记录支付流水
        Pay pay = payMapper.getByOrderId(order.getId());
        if (pay != null) {
            PayRecord payRecord = new PayRecord();
            payRecord.setPayId(pay.getId());
            payRecord.setOrderId(order.getId());
            payRecord.setRecordNo("PR" + System.currentTimeMillis());
            payRecord.setContent("用户取消订单，关闭支付单");
            payRecord.setStatus(PayStatusConstant.CLOSED);
            payRecord.setChannelResponse("用户主动取消订单");
            payRecord.setCreateUser(buyerId);
            payRecord.setUpdateUser(buyerId);
            payRecordMapper.insert(payRecord);
        }
    }

    /**
     * 买家确认完成订单
     */
    @Override
    @Transactional
    public void complete(Long id) {
        Long buyerId = getCurrentUserId();

        Order order = orderMapper.getByIdAndBuyerId(id, buyerId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        if (!OrderStatusConstant.PAID.equals(order.getStatus())) {
            throw new BaseException("当前订单不可完成");
        }

        int rows = orderMapper.completeOrder(
                id,
                buyerId,
                OrderStatusConstant.PAID,
                OrderStatusConstant.COMPLETED,
                LocalDateTime.now()
        );

        if (rows == 0) {
            throw new BaseException("确认完成失败，订单状态异常");
        }

        // 商品改为已售出
        goodsMapper.soldGoods(
                order.getGoodsId(),
                order.getId(),
                GoodsStatusConstant.LOCKED,
                GoodsStatusConstant.SOLD
        );
    }

    /**
     * 分页查询订单
     * type = 1 买家订单
     * type = 2 卖家订单
     */
    @Override
    public PageResult pageQuery(OrderPageQueryDTO dto) {
        Long currentId = getCurrentUserId();

        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<OrderDetailVO> page;

        if (dto.getType() != null && dto.getType() == 2) {
            page = (Page<OrderDetailVO>) orderMapper.pageQuerySell(dto, currentId);
        } else {
            page = (Page<OrderDetailVO>) orderMapper.pageQueryBuy(dto, currentId);
        }

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 订单详情
     * 只有买家本人或卖家本人能查看
     */
    @Override
    public OrderDetailVO detail(Long id) {
        Long currentId = getCurrentUserId();

        OrderDetailVO vo = orderMapper.getDetailById(id);
        if (vo == null) {
            throw new BaseException("订单不存在");
        }

        if (!currentId.equals(vo.getBuyerId()) && !currentId.equals(vo.getSellerId())) {
            throw new BaseException("无权查看该订单");
        }

        return vo;
    }
}