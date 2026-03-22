package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.goods.mapper.GoodsImageMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.goods.service.UserGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.UserGoodsDetailVO;
import com.zhuanzhuan.vo.UserGoodsPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户端商品服务实现。
 */
@Service
public class UserGoodsServiceImpl implements UserGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    /**
     * 用户端分页查询商品列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult page(GoodsPageQueryDTO dto) {
        // 1. 准备查询参数。
        GoodsPageQueryDTO queryDTO = dto == null ? new GoodsPageQueryDTO() : dto;

        // 2. 执行分页查询。
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<UserGoodsPageVO> records = goodsMapper.pageUser(queryDTO);
        Page<UserGoodsPageVO> pageInfo = (Page<UserGoodsPageVO>) records;

        // 3. 返回分页结果。
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 用户端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @Override
    public UserGoodsDetailVO getDetail(Long goodsId) {
        // 1. 查询商品详情。
        UserGoodsDetailVO detailVO = goodsMapper.detailUser(goodsId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 校验用户端可见状态。
        if (!GoodsConstant.STATUS_ON_SALE.equals(detailVO.getStatus())
                && !GoodsConstant.STATUS_LOCKED.equals(detailVO.getStatus())
                && !GoodsConstant.STATUS_SOLD.equals(detailVO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 3. 补充图片并更新浏览量。
        detailVO.setImages(goodsImageMapper.selectByGoodsId(goodsId));
        goodsMapper.increaseViewCount(goodsId, 1);
        detailVO.setViewCount(detailVO.getViewCount() == null ? 1 : detailVO.getViewCount() + 1);

        // 4. 返回商品详情。
        return detailVO;
    }
}
