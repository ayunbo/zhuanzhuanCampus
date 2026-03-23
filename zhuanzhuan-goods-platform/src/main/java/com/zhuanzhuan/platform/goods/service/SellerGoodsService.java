package com.zhuanzhuan.platform.goods.service;

import com.zhuanzhuan.dto.GoodsSaveDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.SellerGoodsDetailVO;

/**
 * 卖家端商品服务接口。
 */
public interface SellerGoodsService {

    /**
     * 创建商品草稿。
     *
     * @param dto 商品保存对象
     * @return 商品 ID
     */
    Long create(GoodsSaveDTO dto);

    /**
     * 修改商品。
     *
     * @param goodsId 商品 ID
     * @param dto 商品保存对象
     */
    void update(Long goodsId, GoodsSaveDTO dto);

    /**
     * 删除商品。
     *
     * @param goodsId 商品 ID
     */
    void delete(Long goodsId);

    /**
     * 分页查询卖家商品。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult page(SellerGoodsPageQueryDTO dto);

    /**
     * 查询卖家商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    SellerGoodsDetailVO getDetail(Long goodsId);

    /**
     * 提交商品审核。
     *
     * @param goodsId 商品 ID
     */
    void submit(Long goodsId);

    /**
     * 重新上架商品。
     *
     * @param goodsId 商品 ID
     */
    void onShelf(Long goodsId);

    /**
     * 下架商品。
     *
     * @param goodsId 商品 ID
     */
    void offShelf(Long goodsId);

    /**
     * 标记商品已售出。
     *
     * @param goodsId 商品 ID
     */
    void sold(Long goodsId);
}
