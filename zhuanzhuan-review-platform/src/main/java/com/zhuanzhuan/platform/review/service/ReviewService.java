package com.zhuanzhuan.platform.review.service;

import com.zhuanzhuan.dto.ReviewPageQueryDTO;
import com.zhuanzhuan.dto.ReviewSubmitDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.ReviewVO;
import com.zhuanzhuan.vo.SellerReviewPageVO;

public interface ReviewService {

    void submit(ReviewSubmitDTO dto);

    ReviewVO getOrderReview(Long orderId);

    PageResult pageByGoodsId(Long goodsId, ReviewPageQueryDTO dto);

    SellerReviewPageVO pageBySellerId(Long sellerId, ReviewPageQueryDTO dto);
}
