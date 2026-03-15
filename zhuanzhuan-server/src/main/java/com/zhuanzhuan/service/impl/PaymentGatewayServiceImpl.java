package com.zhuanzhuan.service.impl;

import com.zhuanzhuan.constant.PayMethodConstant;
import com.zhuanzhuan.dto.PayCreateDTO;
import com.zhuanzhuan.dto.PaySubmitDTO;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.service.PayService;
import com.zhuanzhuan.service.PaymentGatewayService;
import com.zhuanzhuan.service.VirtualWalletPayService;
import com.zhuanzhuan.vo.PayCreateVO;
import com.zhuanzhuan.vo.VirtualWalletLaunchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Autowired
    private PayService payService;

    @Autowired
    private VirtualWalletPayService virtualWalletPayService;

    @Override
    public PayCreateVO submit(PayCreateDTO dto) {
        if (dto == null || dto.getOrderId() == null || dto.getPayMethod() == null) {
            throw new BaseException("支付参数不完整");
        }

        if (PayMethodConstant.MOCK.equals(dto.getPayMethod())) {
            PaySubmitDTO paySubmitDTO = new PaySubmitDTO();
            paySubmitDTO.setOrderId(dto.getOrderId());
            payService.mockPay(paySubmitDTO);

            PayCreateVO vo = new PayCreateVO();
            vo.setOrderId(dto.getOrderId());
            vo.setPayMethod(PayMethodConstant.MOCK);
            vo.setPayStatus(1);
            vo.setAction("direct_success");
            vo.setMessage("模拟支付成功");
            return vo;
        }

        if (PayMethodConstant.VIRTUAL_WALLET.equals(dto.getPayMethod())) {
            VirtualWalletLaunchVO walletLaunchVO = virtualWalletPayService.createPay(dto.getOrderId());

            PayCreateVO vo = new PayCreateVO();
            vo.setOrderId(walletLaunchVO.getOrderId());
            vo.setPayMethod(PayMethodConstant.VIRTUAL_WALLET);
            vo.setPayStatus(walletLaunchVO.getPayStatus());
            vo.setRequestNo(walletLaunchVO.getRequestNo());
            vo.setWalletScheme(walletLaunchVO.getWalletScheme());

            boolean androidClient = "android".equalsIgnoreCase(dto.getClientType());
            boolean hasReturnUrl = StringUtils.hasText(dto.getReturnUrl());

            if (androidClient) {
                vo.setAction("open_wallet");
                vo.setMessage("请拉起虚拟钱包完成支付");
                return vo;
            }

            vo.setAction("redirect_page");
            vo.setRedirectUrl(hasReturnUrl ? dto.getReturnUrl() : walletLaunchVO.getReturnUrl());
            vo.setMessage("请跳转到虚拟钱包收银台或拉起钱包应用");
            return vo;
        }

        throw new BaseException("不支持的支付方式");
    }
}
