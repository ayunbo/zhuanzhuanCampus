package com.zhuanzhuan.platform.goods.service;

import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.UserGoodsDetailVO;

/**
 * 用户端商品服务接口。
 */
public interface UserGoodsService {

    /**
     * 用户端分页查询商品列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult page(GoodsPageQueryDTO dto);

    /**
     * 用户端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    UserGoodsDetailVO getDetail(Long goodsId);
}
