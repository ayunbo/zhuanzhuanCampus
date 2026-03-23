package com.zhuanzhuan.platform.goods.service;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;

/**
 * 管理端商品服务接口。
 */
public interface AdminGoodsService {

    /**
     * 分页查询商品。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult page(AdminGoodsPageQueryDTO dto);

    /**
     * 查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    AdminGoodsDetailVO getDetail(Long goodsId);

    /**
     * 审核商品。
     *
     * @param goodsId 商品 ID
     * @param dto 审核参数
     */
    void audit(Long goodsId, AdminGoodsAuditDTO dto);
}
