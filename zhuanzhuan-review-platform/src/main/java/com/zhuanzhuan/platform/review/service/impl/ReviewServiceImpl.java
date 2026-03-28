package com.zhuanzhuan.platform.review.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.OrderStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.ReviewPageQueryDTO;
import com.zhuanzhuan.dto.ReviewSubmitDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.entity.Review;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.review.mapper.ReviewMapper;
import com.zhuanzhuan.platform.review.service.ReviewService;
import com.zhuanzhuan.platform.trade.mapper.OrderMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.utils.ValidationRuleUtil;
import com.zhuanzhuan.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final int MAX_REVIEW_IMAGE_COUNT = 6;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void submit(ReviewSubmitDTO dto) {
        if (dto == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
        if (dto.getOrderId() == null) {
            throw new BaseException(MessageConstant.REVIEW_ORDER_ID_REQUIRED);
        }
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BaseException(MessageConstant.REVIEW_SCORE_INVALID);
        }

        Long currentUserId = getCurrentUserId();
        Order order = orderMapper.getByIdAndBuyerId(dto.getOrderId(), currentUserId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!OrderStatusConstant.COMPLETED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.REVIEW_ORDER_NOT_COMPLETED);
        }

        Review existingReview = reviewMapper.getByOrderId(order.getId());
        if (existingReview != null) {
            throw new BaseException(MessageConstant.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setGoodsId(order.getGoodsId());
        review.setUserId(currentUserId);
        review.setTargetUserId(order.getSellerId());
        review.setScore(dto.getScore());
        review.setContent(normalizeContent(dto.getContent()));
        review.setImages(normalizeImages(dto.getImages()));
        review.setAnonymous(normalizeAnonymous(dto.getAnonymous()));
        review.setCreateUser(currentUserId);
        review.setUpdateUser(currentUserId);

        int rows = reviewMapper.insert(review);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.REVIEW_SUBMIT_FAILED);
        }

        int statRows = userMapper.increaseReviewStats(order.getSellerId(), dto.getScore(), currentUserId);
        if (statRows <= 0) {
            throw new BaseException(MessageConstant.REVIEW_SUBMIT_FAILED);
        }
    }

    @Override
    public ReviewVO getOrderReview(Long orderId) {
        if (orderId == null) {
            throw new BaseException(MessageConstant.REVIEW_ORDER_ID_REQUIRED);
        }

        Long currentUserId = getCurrentUserId();
        Order order = orderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!currentUserId.equals(order.getBuyerId()) && !currentUserId.equals(order.getSellerId())) {
            throw new BaseException(MessageConstant.REVIEW_NOT_ALLOWED);
        }

        ReviewVO reviewVO = reviewMapper.getOrderReview(orderId);
        if (reviewVO == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }
        maskAnonymousReviewer(reviewVO, currentUserId);
        return reviewVO;
    }

    @Override
    public PageResult pageByGoodsId(Long goodsId, ReviewPageQueryDTO dto) {
        if (goodsId == null) {
            throw new BaseException(MessageConstant.REVIEW_GOODS_ID_REQUIRED);
        }

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        ReviewPageQueryDTO queryDTO = dto == null ? new ReviewPageQueryDTO() : dto;
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<ReviewVO> records = reviewMapper.pageByGoodsId(goodsId);
        records.forEach(item -> maskAnonymousReviewer(item, null));

        Page<ReviewVO> pageInfo = (Page<ReviewVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    private Long getCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        return currentId;
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String normalized = content.trim();
        if (normalized.length() > 500) {
            throw new BaseException(MessageConstant.REVIEW_CONTENT_TOO_LONG);
        }
        return normalized;
    }

    private Integer normalizeAnonymous(Integer anonymous) {
        if (anonymous == null) {
            return 0;
        }
        if (anonymous != 0 && anonymous != 1) {
            throw new BaseException(MessageConstant.REVIEW_ANONYMOUS_INVALID);
        }
        return anonymous;
    }

    private String normalizeImages(String images) {
        if (!StringUtils.hasText(images)) {
            return null;
        }

        List<String> imageUrls = Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        if (imageUrls.isEmpty()) {
            return null;
        }
        if (imageUrls.size() > MAX_REVIEW_IMAGE_COUNT) {
            throw new BaseException(MessageConstant.REVIEW_IMAGES_TOO_MANY);
        }
        for (String imageUrl : imageUrls) {
            if (!ValidationRuleUtil.isValidHttpUrl(imageUrl)) {
                throw new BaseException(MessageConstant.REVIEW_IMAGE_URL_INVALID);
            }
        }
        return String.join(",", imageUrls);
    }

    private void maskAnonymousReviewer(ReviewVO reviewVO, Long currentUserId) {
        if (reviewVO == null) {
            return;
        }
        if (reviewVO.getAnonymous() != null && reviewVO.getAnonymous() == 1) {
            if (currentUserId == null || !currentUserId.equals(reviewVO.getUserId())) {
                reviewVO.setReviewerName("匿名用户");
                reviewVO.setReviewerAvatar(null);
                return;
            }
        }
        if (!StringUtils.hasText(reviewVO.getReviewerName())) {
            reviewVO.setReviewerName("用户" + reviewVO.getUserId());
        }
    }
}
