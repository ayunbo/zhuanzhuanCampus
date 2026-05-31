package com.zhuanzhuan.task;

import com.zhuanzhuan.constant.GoodsStatusConstant;
import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.trade.mapper.OrderMapper;
import com.zhuanzhuan.platform.trade.mapper.PayMapper;
import com.zhuanzhuan.platform.trade.mapper.PayRecordMapper;
import com.zhuanzhuan.service.notify.NoticePublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private PayRecordMapper payRecordMapper;
    @Autowired
    private NoticePublishService noticePublishService;

    /**
     * 每分钟扫描一次超时未支付订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanTimeoutOrder() {
        List<Order> orders = orderMapper.listTimeoutOrders(LocalDateTime.now());

        if (orders == null || orders.isEmpty()) {
            return;
        }

        log.info("扫描到超时订单数量：{}", orders.size());

        for (Order order : orders) {
            try {
                handleTimeoutOrder(order);
            } catch (Exception e) {
                log.error("处理超时订单失败，orderId={}", order.getId(), e);
            }
        }
    }

    @Transactional
    public void handleTimeoutOrder(Order order) {
        int rows = orderMapper.timeoutCloseOrder(
                order.getId(),
                OrderStatusConstant.PENDING_PAY,
                OrderStatusConstant.TIMEOUT_CLOSED,
                LocalDateTime.now()
        );

        if (rows == 0) {
            return;
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
            payRecord.setContent("超时未支付，系统自动关闭支付单");
            payRecord.setStatus(PayStatusConstant.CLOSED);
            payRecord.setChannelResponse("系统定时任务自动关闭");
            payRecord.setCreateUser(0L);
            payRecord.setUpdateUser(0L);
            payRecordMapper.insert(payRecord);
        }

        noticePublishService.publishOrderStatusChange(
                order.getBuyerId(),
                order.getId(),
                "订单超时关闭",
                "你的订单因超过 30 分钟未支付已自动关闭，商品已恢复上架。"
        );
        noticePublishService.publishOrderStatusChange(
                order.getSellerId(),
                order.getId(),
                "订单超时关闭",
                "买家超时未支付，订单已自动关闭，商品已恢复上架。"
        );

        log.info("超时订单已关闭，orderId={}", order.getId());
    }
}
