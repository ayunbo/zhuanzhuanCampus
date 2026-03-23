package com.zhuanzhuan.platform.collect.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.FavoritePageQueryDTO;
import com.zhuanzhuan.entity.Favorite;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.collect.mapper.FavoriteMapper;
import com.zhuanzhuan.platform.collect.service.FavoriteService;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.FavoriteStatusVO;
import com.zhuanzhuan.vo.FavoriteToggleVO;
import com.zhuanzhuan.vo.UserFavoritePageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public FavoriteToggleVO collect(Long goodsId) {
        validateGoodsId(goodsId);
        Long userId = requireCurrentUserId();

        Goods goods = requireGoods(goodsId);
        validateCollectibleStatus(goods.getStatus());

        Favorite favorite = Favorite.builder()
                .id(IdGenerator.nextId())
                .userId(userId)
                .goodsId(goodsId)
                .build();

        int inserted = favoriteMapper.insertIgnore(favorite);
        if (inserted <= 0) {
            throw new BaseException(MessageConstant.FAVORITE_ALREADY_EXISTS);
        }

        goodsMapper.adjustFavoriteCount(goodsId, 1);
        return buildToggleVO(goodsId, true);
    }

    @Override
    public FavoriteToggleVO cancel(Long goodsId) {
        validateGoodsId(goodsId);
        Long userId = requireCurrentUserId();

        int deleted = favoriteMapper.deleteByUserIdAndGoodsId(userId, goodsId);
        if (deleted > 0) {
            goodsMapper.adjustFavoriteCount(goodsId, -1);
        }

        return buildToggleVO(goodsId, false);
    }

    @Override
    public FavoriteStatusVO status(Long goodsId) {
        validateGoodsId(goodsId);
        Long userId = requireCurrentUserId();
        requireGoods(goodsId);

        Integer count = favoriteMapper.countByUserIdAndGoodsId(userId, goodsId);
        return FavoriteStatusVO.builder()
                .goodsId(goodsId)
                .favorited(count != null && count > 0)
                .build();
    }

    @Override
    public PageResult page(FavoritePageQueryDTO dto) {
        Long userId = requireCurrentUserId();
        FavoritePageQueryDTO queryDTO = dto == null ? new FavoritePageQueryDTO() : dto;

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<UserFavoritePageVO> records = favoriteMapper.pageByUserId(userId, queryDTO.getSortType());
        Page<UserFavoritePageVO> pageInfo = (Page<UserFavoritePageVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    private FavoriteToggleVO buildToggleVO(Long goodsId, boolean favorited) {
        Goods latestGoods = goodsMapper.selectById(goodsId);
        Integer favoriteCount = latestGoods == null || latestGoods.getFavoriteCount() == null
                ? 0
                : latestGoods.getFavoriteCount();

        return FavoriteToggleVO.builder()
                .goodsId(goodsId)
                .favorited(favorited)
                .favoriteCount(favoriteCount)
                .build();
    }

    private void validateGoodsId(Long goodsId) {
        if (goodsId == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
    }

    private Long requireCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        return userId;
    }

    private Goods requireGoods(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        return goods;
    }

    private void validateCollectibleStatus(Integer status) {
        if (GoodsConstant.STATUS_ON_SALE.equals(status) || GoodsConstant.STATUS_LOCKED.equals(status)) {
            return;
        }
        throw new BaseException(MessageConstant.FAVORITE_NOT_SUPPORTED_STATUS);
    }
}
