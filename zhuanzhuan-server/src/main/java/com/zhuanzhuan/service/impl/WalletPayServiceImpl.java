package com.zhuanzhuan.service.impl;

import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.constant.PayMethodConstant;
import com.zhuanzhuan.constant.PayStatusConstant;
import com.zhuanzhuan.dto.WalletPayConfirmDTO;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Pay;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.entity.WalletAccount;
import com.zhuanzhuan.entity.WalletBankCard;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.OrderMapper;
import com.zhuanzhuan.mapper.PayMapper;
import com.zhuanzhuan.mapper.PayRecordMapper;
import com.zhuanzhuan.mapper.WalletAccountMapper;
import com.zhuanzhuan.mapper.WalletBankCardMapper;
import com.zhuanzhuan.service.WalletPayService;
import com.zhuanzhuan.vo.WalletBankCardVO;
import com.zhuanzhuan.vo.WalletInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletPayServiceImpl implements WalletPayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Autowired
    private WalletAccountMapper walletAccountMapper;

    @Autowired
    private WalletBankCardMapper walletBankCardMapper;

    @Override
    public WalletInfoVO getWalletInfo(Long orderId, String loginName) {
        if (orderId == null || !StringUtils.hasText(loginName)) {
            throw new BaseException("钱包页面参数不完整");
        }

        Order order = getPayableOrder(orderId);
        Pay pay = getPendingPay(orderId);
        WalletAccount walletAccount = getWalletAccount(loginName);
        List<WalletBankCard> bankCards = walletBankCardMapper.listByWalletAccountId(walletAccount.getId());

        WalletInfoVO vo = new WalletInfoVO();
        vo.setOrderId(order.getId());
        vo.setRequestNo(pay.getRequestNo());
        vo.setLoginName(walletAccount.getLoginName());
        vo.setWalletUserNo(walletAccount.getWalletUserNo());
        vo.setWalletName(walletAccount.getWalletName());
        vo.setAmount(order.getAmount());
        vo.setWalletBalance(walletAccount.getBalance());
        vo.setBankCards(bankCards.stream().map(this::toCardVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public void confirmPay(WalletPayConfirmDTO dto) {
        if (dto == null || dto.getOrderId() == null || !StringUtils.hasText(dto.getLoginName())
                || !StringUtils.hasText(dto.getPayPassword()) || dto.getPayChannel() == null) {
            throw new BaseException("支付参数不完整");
        }

        Order order = getPayableOrder(dto.getOrderId());
        Pay pay = getPendingPay(dto.getOrderId());
        WalletAccount walletAccount = getWalletAccount(dto.getLoginName());

        String encryptedPassword = DigestUtils.md5DigestAsHex(dto.getPayPassword().trim().getBytes());
        if (!encryptedPassword.equals(walletAccount.getPayPassword())) {
            insertWalletFailRecord(pay, order, walletAccount, dto, "支付密码错误");
            throw new BaseException("支付密码错误");
        }

        if (dto.getPayChannel() == 1) {
            int rows = walletAccountMapper.deductBalance(walletAccount.getId(), order.getAmount());
            if (rows == 0) {
                insertWalletFailRecord(pay, order, walletAccount, dto, "钱包余额不足");
                throw new BaseException("钱包余额不足");
            }
            completePlatformPay(pay, order, walletAccount.getWalletUserNo(), "钱包余额支付成功");
            return;
        }

        if (dto.getPayChannel() == 2) {
            if (dto.getBankCardId() == null) {
                throw new BaseException("请选择银行卡");
            }
            WalletBankCard bankCard = walletBankCardMapper.getById(dto.getBankCardId());
            if (bankCard == null || !bankCard.getWalletAccountId().equals(walletAccount.getId())) {
                throw new BaseException("银行卡不存在");
            }

            int rows = walletBankCardMapper.deductBalance(bankCard.getId(), order.getAmount());
            if (rows == 0) {
                insertWalletFailRecord(pay, order, walletAccount, dto, "银行卡余额不足");
                throw new BaseException("银行卡余额不足");
            }
            completePlatformPay(pay, order, walletAccount.getWalletUserNo(),
                    "银行卡支付成功:" + bankCard.getCardNoMask());
            return;
        }

        throw new BaseException("不支持的支付渠道");
    }

    private void completePlatformPay(Pay pay, Order order, String walletUserNo, String message) {
        LocalDateTime now = LocalDateTime.now();

        int payRows = payMapper.updateLaunchInfo(pay.getId(),
                StringUtils.hasText(pay.getRequestNo()) ? pay.getRequestNo() : "VW" + System.currentTimeMillis() + order.getId(),
                PayMethodConstant.VIRTUAL_WALLET,
                order.getBuyerId());
        if (payRows == 0 && !PayStatusConstant.PENDING.equals(pay.getStatus())) {
            throw new BaseException("支付单状态异常");
        }

        int successRows = payMapper.paySuccess(pay.getId(), PayStatusConstant.PENDING, PayStatusConstant.SUCCESS, now);
        if (successRows == 0) {
            throw new BaseException("支付单状态异常");
        }

        int orderRows = orderMapper.paySuccess(order.getId(), OrderStatusConstant.PENDING_PAY, OrderStatusConstant.PAID, now);
        if (orderRows == 0) {
            throw new BaseException("订单状态异常");
        }

        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent(message);
        payRecord.setStatus(PayStatusConstant.SUCCESS);
        payRecord.setChannelResponse("walletUserNo=" + walletUserNo);
        payRecord.setCreateUser(order.getBuyerId());
        payRecord.setUpdateUser(order.getBuyerId());
        payRecordMapper.insert(payRecord);
    }

    private void insertWalletFailRecord(Pay pay, Order order, WalletAccount walletAccount,
                                        WalletPayConfirmDTO dto, String reason) {
        PayRecord payRecord = new PayRecord();
        payRecord.setPayId(pay.getId());
        payRecord.setOrderId(order.getId());
        payRecord.setRecordNo("PR" + System.currentTimeMillis());
        payRecord.setContent("虚拟钱包支付失败");
        payRecord.setStatus(PayStatusConstant.FAIL);
        payRecord.setChannelResponse("walletUserNo=" + walletAccount.getWalletUserNo()
                + ", channel=" + dto.getPayChannel()
                + ", reason=" + reason);
        payRecord.setCreateUser(order.getBuyerId());
        payRecord.setUpdateUser(order.getBuyerId());
        payRecordMapper.insert(payRecord);
    }

    private WalletBankCardVO toCardVO(WalletBankCard bankCard) {
        WalletBankCardVO vo = new WalletBankCardVO();
        BeanUtils.copyProperties(bankCard, vo);
        return vo;
    }

    private WalletAccount getWalletAccount(String loginName) {
        WalletAccount walletAccount = walletAccountMapper.getByLoginName(loginName);
        if (walletAccount == null) {
            throw new BaseException("钱包账户不存在");
        }
        return walletAccount;
    }

    private Order getPayableOrder(Long orderId) {
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!OrderStatusConstant.PENDING_PAY.equals(order.getStatus())) {
            throw new BaseException("当前订单不可支付");
        }
        return order;
    }

    private Pay getPendingPay(Long orderId) {
        Pay pay = payMapper.getByOrderId(orderId);
        if (pay == null) {
            throw new BaseException("支付单不存在");
        }
        if (!PayStatusConstant.PENDING.equals(pay.getStatus())) {
            throw new BaseException("支付单状态异常");
        }
        return pay;
    }
}
