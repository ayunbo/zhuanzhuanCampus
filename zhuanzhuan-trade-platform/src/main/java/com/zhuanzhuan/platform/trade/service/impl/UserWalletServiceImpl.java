package com.zhuanzhuan.platform.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.WalletAccountOpenDTO;
import com.zhuanzhuan.dto.WalletBankCardBindDTO;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.entity.WalletAccount;
import com.zhuanzhuan.entity.WalletBankCard;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.trade.mapper.PayRecordMapper;
import com.zhuanzhuan.platform.trade.mapper.WalletAccountMapper;
import com.zhuanzhuan.platform.trade.mapper.WalletBankCardMapper;
import com.zhuanzhuan.platform.trade.service.UserWalletService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.WalletBankCardVO;
import com.zhuanzhuan.vo.WalletOverviewVO;
import com.zhuanzhuan.vo.WalletTransactionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserWalletServiceImpl implements UserWalletService {

    @Autowired
    private WalletAccountMapper walletAccountMapper;

    @Autowired
    private WalletBankCardMapper walletBankCardMapper;

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public WalletOverviewVO overview() {
        User currentUser = getCurrentUser();
        WalletOverviewVO overviewVO = new WalletOverviewVO();
        overviewVO.setLoginName(currentUser.getStudentNo());
        overviewVO.setWalletBalance(BigDecimal.ZERO);
        overviewVO.setWalletStatus(0);
        overviewVO.setBankCards(Collections.emptyList());

        WalletAccount walletAccount = walletAccountMapper.getByLoginName(currentUser.getStudentNo());
        if (walletAccount == null) {
            return overviewVO;
        }

        List<WalletBankCard> bankCards = walletBankCardMapper.listByWalletAccountId(walletAccount.getId());
        overviewVO.setWalletUserNo(walletAccount.getWalletUserNo());
        overviewVO.setWalletName(walletAccount.getWalletName());
        overviewVO.setWalletBalance(walletAccount.getBalance());
        overviewVO.setWalletStatus(walletAccount.getStatus());
        overviewVO.setBankCards(bankCards.stream().map(this::toCardVO).collect(Collectors.toList()));
        return overviewVO;
    }

    @Override
    public PageResult records(Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);

        PageHelper.startPage(currentPage, currentPageSize);
        Page<WalletTransactionVO> pageInfo =
                (Page<WalletTransactionVO>) payRecordMapper.pageWalletRecords(getCurrentUserId());
        return new PageResult(pageInfo.getTotal(), pageInfo.getResult());
    }

    @Override
    @Transactional
    public void openAccount(WalletAccountOpenDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getPayPassword())) {
            throw new BaseException("请先填写钱包支付密码");
        }

        User currentUser = getCurrentUser();
        String loginName = currentUser.getStudentNo();
        if (!StringUtils.hasText(loginName)) {
            throw new BaseException("当前账号缺少学号，无法开通钱包");
        }

        if (walletAccountMapper.getAnyByLoginName(loginName) != null) {
            throw new BaseException("当前账号已开通虚拟钱包");
        }

        String phone = StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : safe(currentUser.getPhone());
        if (!StringUtils.hasText(phone)) {
            throw new BaseException("请填写钱包手机号");
        }
        if (walletAccountMapper.getByPhone(phone) != null) {
            throw new BaseException("该手机号已绑定其他钱包账户");
        }

        WalletAccount walletAccount = new WalletAccount();
        walletAccount.setWalletUserNo(buildWalletUserNo(getCurrentUserId()));
        walletAccount.setLoginName(loginName);
        walletAccount.setWalletName(resolveWalletName(dto.getWalletName(), currentUser));
        walletAccount.setPhone(phone);
        walletAccount.setPayPassword(md5(dto.getPayPassword().trim()));
        walletAccount.setBalance(normalizeAmount(dto.getBalance()));
        walletAccount.setStatus(1);
        walletAccount.setLastLoginTime(LocalDateTime.now());
        walletAccount.setCreateUser(getCurrentUserId());
        walletAccount.setUpdateUser(getCurrentUserId());
        walletAccountMapper.insert(walletAccount);
    }

    @Override
    @Transactional
    public void bindBankCard(WalletBankCardBindDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getBankName()) || !StringUtils.hasText(dto.getCardNo())) {
            throw new BaseException("请完善银行卡信息");
        }

        WalletAccount walletAccount = getCurrentWalletAccount();
        String cardNo = dto.getCardNo().replace(" ", "").trim();
        if (cardNo.length() < 8) {
            throw new BaseException("银行卡号格式不正确");
        }
        if (walletBankCardMapper.getByCardNo(cardNo) != null) {
            throw new BaseException("该银行卡已被绑定");
        }

        List<WalletBankCard> existingCards = walletBankCardMapper.listByWalletAccountId(walletAccount.getId());
        boolean shouldSetDefault = (dto.getIsDefault() != null && dto.getIsDefault() == 1) || existingCards.isEmpty();
        if (shouldSetDefault) {
            walletBankCardMapper.clearDefaultByWalletAccountId(walletAccount.getId(), getCurrentUserId());
        }

        WalletBankCard bankCard = new WalletBankCard();
        bankCard.setWalletAccountId(walletAccount.getId());
        bankCard.setBankName(dto.getBankName().trim());
        bankCard.setCardHolder(resolveCardHolder(dto.getCardHolder(), walletAccount));
        bankCard.setCardNo(cardNo);
        bankCard.setCardNoMask(maskCardNo(cardNo));
        bankCard.setCardType(dto.getCardType() == null ? 1 : dto.getCardType());
        bankCard.setBalance(normalizeAmount(dto.getBalance()));
        bankCard.setIsDefault(shouldSetDefault ? 1 : 0);
        bankCard.setStatus(1);
        bankCard.setBindTime(LocalDateTime.now());
        bankCard.setCreateUser(getCurrentUserId());
        bankCard.setUpdateUser(getCurrentUserId());
        walletBankCardMapper.insert(bankCard);
    }

    @Override
    @Transactional
    public void setDefaultBankCard(Long bankCardId) {
        if (bankCardId == null) {
            throw new BaseException("请选择要设置的银行卡");
        }

        WalletAccount walletAccount = getCurrentWalletAccount();
        WalletBankCard bankCard = walletBankCardMapper.getById(bankCardId);
        if (bankCard == null || !walletAccount.getId().equals(bankCard.getWalletAccountId())) {
            throw new BaseException("银行卡不存在");
        }

        walletBankCardMapper.clearDefaultByWalletAccountId(walletAccount.getId(), getCurrentUserId());
        walletBankCardMapper.setDefault(bankCardId, walletAccount.getId(), getCurrentUserId());
    }

    private WalletBankCardVO toCardVO(WalletBankCard bankCard) {
        WalletBankCardVO walletBankCardVO = new WalletBankCardVO();
        BeanUtils.copyProperties(bankCard, walletBankCardVO);
        return walletBankCardVO;
    }

    private WalletAccount getCurrentWalletAccount() {
        User currentUser = getCurrentUser();
        WalletAccount walletAccount = walletAccountMapper.getByLoginName(currentUser.getStudentNo());
        if (walletAccount == null) {
            throw new BaseException("钱包账户不存在，请先开通钱包");
        }
        return walletAccount;
    }

    private User getCurrentUser() {
        User currentUser = userMapper.getById(getCurrentUserId());
        if (currentUser == null) {
            throw new BaseException("当前用户不存在");
        }
        return currentUser;
    }

    private Long getCurrentUserId() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException("用户未登录");
        }
        return currentUserId;
    }

    private String resolveWalletName(String walletName, User currentUser) {
        if (StringUtils.hasText(walletName)) {
            return walletName.trim();
        }
        if (StringUtils.hasText(currentUser.getName())) {
            return currentUser.getName().trim();
        }
        return currentUser.getStudentNo();
    }

    private String resolveCardHolder(String cardHolder, WalletAccount walletAccount) {
        if (StringUtils.hasText(cardHolder)) {
            return cardHolder.trim();
        }
        if (StringUtils.hasText(walletAccount.getWalletName())) {
            return walletAccount.getWalletName().trim();
        }
        return walletAccount.getLoginName();
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException("金额不能为负数");
        }
        return amount;
    }

    private String maskCardNo(String cardNo) {
        String suffix = cardNo.substring(cardNo.length() - 4);
        return "**** **** **** " + suffix;
    }

    private String buildWalletUserNo(Long userId) {
        return "WU" + System.currentTimeMillis() + userId;
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
