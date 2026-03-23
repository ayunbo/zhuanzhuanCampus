package com.zhuanzhuan.platform.review.mapper;

import com.zhuanzhuan.entity.Review;
import com.zhuanzhuan.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    int insert(Review review);

    Review getByOrderId(@Param("orderId") Long orderId);

    ReviewVO getOrderReview(@Param("orderId") Long orderId);

    List<ReviewVO> pageByGoodsId(@Param("goodsId") Long goodsId);
}
