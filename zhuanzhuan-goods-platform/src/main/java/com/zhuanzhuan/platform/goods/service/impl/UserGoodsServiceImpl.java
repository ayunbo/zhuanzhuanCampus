package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsImageMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.goods.service.GoodsStatsAsyncService;
import com.zhuanzhuan.platform.goods.service.UserGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.SellerSpaceVO;
import com.zhuanzhuan.vo.UserGoodsDetailVO;
import com.zhuanzhuan.vo.UserGoodsPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsStatsAsyncService goodsStatsAsyncService;

    /**
     * 用户端分页查询商品列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult page(GoodsPageQueryDTO dto) {
        GoodsPageQueryDTO queryDTO = dto == null ? new GoodsPageQueryDTO() : dto;
        validateUserGoodsStatus(queryDTO.getStatus());

        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<UserGoodsPageVO> records = goodsMapper.pageUser(queryDTO);
        Page<UserGoodsPageVO> pageInfo = (Page<UserGoodsPageVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public SellerSpaceVO getSellerSpace(Long sellerId) {
        if (sellerId == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = userMapper.getById(sellerId);
        if (seller == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        SellerSpaceVO vo = new SellerSpaceVO();
        vo.setSellerId(seller.getId());
        vo.setSellerName(StringUtils.hasText(seller.getName()) ? seller.getName() : ("卖家" + seller.getId()));
        vo.setSellerAvatar(seller.getAvatar());
        vo.setSellerCampus(seller.getCampus());
        vo.setSellerIntro(seller.getIntro());
        vo.setSellerScoreAvg(seller.getScoreAvg());
        vo.setSellerReviewCount(seller.getReviewCount() == null ? 0 : seller.getReviewCount());
        vo.setOnSaleCount(safeCount(goodsMapper.countBySellerIdAndStatus(sellerId, GoodsConstant.STATUS_ON_SALE)));
        vo.setSoldCount(safeCount(goodsMapper.countBySellerIdAndStatus(sellerId, GoodsConstant.STATUS_SOLD)));
        return vo;
    }

    /**
     * 用户端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @Override
    public UserGoodsDetailVO getDetail(Long goodsId) {
        UserGoodsDetailVO detailVO = goodsMapper.detailUser(goodsId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        if (!GoodsConstant.STATUS_ON_SALE.equals(detailVO.getStatus())
                && !GoodsConstant.STATUS_LOCKED.equals(detailVO.getStatus())
                && !GoodsConstant.STATUS_SOLD.equals(detailVO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        detailVO.setImages(goodsImageMapper.selectByGoodsId(goodsId));
        goodsStatsAsyncService.increaseViewCountAsync(goodsId, 1);
        detailVO.setViewCount(detailVO.getViewCount() == null ? 1 : detailVO.getViewCount() + 1);
        return detailVO;
    }

    private void validateUserGoodsStatus(Integer status) {
        if (status == null) {
            return;
        }
        if (!GoodsConstant.STATUS_ON_SALE.equals(status) && !GoodsConstant.STATUS_SOLD.equals(status)) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }
    }

    private Long safeCount(Long value) {
        return value == null ? 0L : value;
    }
}
