package com.zhuanzhuan.service.sellerauth;

import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.dto.SellerAuthApplyDTO;
import com.zhuanzhuan.dto.SellerAuthAuditDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.SellerAuthResultVO;

/**
 * 卖家申请与审核业务服务接口。
 */
public interface SellerAuthService {

    /**
     * 用户提交卖家申请。
     */
    void submitSellerAuth(SellerAuthApplyDTO sellerAuthApplyDTO);

    /**
     * 查询当前用户最近一次卖家申请结果。
     */
    SellerAuthResultVO getCurrentSellerAuthResult();

    /**
     * 校验当前用户是否为卖家。
     */
    void checkCurrentUserIsSeller();

    /**
     * 管理员分页查询卖家申请。
     */
    PageResult pageQuerySellerAuth(AdminSellerAuthPageQueryDTO pageQueryDTO);

    /**
     * 管理员审核卖家申请。
     */
    void auditSellerAuth(SellerAuthAuditDTO sellerAuthAuditDTO);
}