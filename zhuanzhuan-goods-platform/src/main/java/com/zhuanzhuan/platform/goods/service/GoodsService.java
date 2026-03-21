package com.zhuanzhuan.platform.goods.service;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsDraftSaveDTO;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsStatUpdateDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.GoodsVO;

/**
 * 商品服务接口。
 */
public interface GoodsService {

    /**
     * 新增商品草稿。
     */
    Long createDraft(GoodsDraftSaveDTO goodsDraftSaveDTO);

    /**
     * 修改商品草稿。
     */
    void updateDraft(Long id, GoodsDraftSaveDTO goodsDraftSaveDTO);

    /**
     * 提交商品审核。
     */
    void submitAudit(Long id);

    /**
     * 上架商品。
     */
    void onShelf(Long id);

    /**
     * 下架商品。
     */
    void offShelf(Long id);

    /**
     * 标记商品已售。
     */
    void markSold(Long id);

    /**
     * 卖家分页查询商品。
     */
    PageResult pageBySeller(SellerGoodsPageQueryDTO pageQueryDTO);

    /**
     * 管理端分页查询商品。
     */
    PageResult adminPage(AdminGoodsPageQueryDTO pageQueryDTO);

    /**
     * 用户分页查询商品。
     */
    PageResult pageForUser(GoodsPageQueryDTO pageQueryDTO);

    /**
     * 查询商品详情。
     */
    GoodsVO getDetail(Long id);

    /**
     * 审核商品。
     */
    void adminAudit(AdminGoodsAuditDTO adminGoodsAuditDTO);

    /**
     * 更新商品统计。
     */
    void updateStats(GoodsStatUpdateDTO goodsStatUpdateDTO);
}
