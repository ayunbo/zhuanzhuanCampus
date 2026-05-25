package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.StatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.GoodsSaveDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.entity.Category;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.entity.GoodsImage;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.goods.mapper.CategoryMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsImageMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.goods.service.SellerGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.utils.AliOssUtil;
import com.zhuanzhuan.utils.ValidationRuleUtil;
import com.zhuanzhuan.vo.SellerGoodsDetailVO;
import com.zhuanzhuan.vo.SellerGoodsPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SellerGoodsServiceImpl implements SellerGoodsService {

    private static final int MAX_GOODS_IMAGE_COUNT = 9;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    @Transactional
    public Long create(GoodsSaveDTO dto) {
        Long currentUserId = requireSellerUserId();
        GoodsSaveDTO saveDTO = dto == null ? new GoodsSaveDTO() : dto;
        List<String> imageUrls = normalizeGoodsImageUrls(saveDTO.getImageUrls());
        validateCategory(saveDTO.getCategoryId());

        Goods goods = Goods.builder()
                .sellerId(currentUserId)
                .categoryId(saveDTO.getCategoryId() == null ? GoodsConstant.DEFAULT_CATEGORY_ID : saveDTO.getCategoryId())
                .title(saveDTO.getTitle() == null ? "" : saveDTO.getTitle())
                .detail(saveDTO.getDetail())
                .price(saveDTO.getPrice() == null ? BigDecimal.ZERO : saveDTO.getPrice())
                .oldPrice(saveDTO.getOldPrice())
                .quality(saveDTO.getQuality() == null ? GoodsConstant.DEFAULT_QUALITY : saveDTO.getQuality())
                .location(saveDTO.getLocation())
                .status(GoodsConstant.STATUS_DRAFT)
                .cover(imageUrls.isEmpty() ? null : imageUrls.get(0))
                .reason(null)
                .auditAdminId(null)
                .auditTime(null)
                .publishTime(null)
                .viewCount(GoodsConstant.INITIAL_VIEW_COUNT)
                .favoriteCount(GoodsConstant.INITIAL_FAVORITE_COUNT)
                .lockOrderId(null)
                .version(GoodsConstant.INITIAL_VERSION)
                .build();
        int rows = goodsMapper.insert(goods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_CREATE_FAILED);
        }

        replaceGoodsImages(goods.getId(), imageUrls);
        return goods.getId();
    }

    @Override
    @Transactional
    public void update(Long goodsId, GoodsSaveDTO dto) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        GoodsSaveDTO saveDTO = dto == null ? new GoodsSaveDTO() : dto;
        List<String> imageUrls = normalizeGoodsImageUrls(saveDTO.getImageUrls());
        validateCategory(saveDTO.getCategoryId());

        Integer targetStatus = GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus())
                ? GoodsConstant.STATUS_DRAFT : currentGoods.getStatus();
        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .sellerId(currentUserId)
                .categoryId(saveDTO.getCategoryId() == null ? GoodsConstant.DEFAULT_CATEGORY_ID : saveDTO.getCategoryId())
                .title(saveDTO.getTitle() == null ? "" : saveDTO.getTitle())
                .detail(saveDTO.getDetail())
                .price(saveDTO.getPrice() == null ? BigDecimal.ZERO : saveDTO.getPrice())
                .oldPrice(saveDTO.getOldPrice())
                .quality(saveDTO.getQuality() == null ? GoodsConstant.DEFAULT_QUALITY : saveDTO.getQuality())
                .location(saveDTO.getLocation())
                .cover(imageUrls.isEmpty() ? null : imageUrls.get(0))
                .status(targetStatus)
                .reason(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getReason())
                .auditAdminId(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getAuditAdminId())
                .auditTime(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getAuditTime())
                .publishTime(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getPublishTime())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
        replaceGoodsImages(goodsId, imageUrls);
    }

    @Override
    @Transactional
    public void delete(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        List<String> imageUrls = getGoodsImageUrls(goodsId);
        goodsImageMapper.deleteByGoodsId(goodsId);
        int rows = goodsMapper.deleteByIdAndSellerId(goodsId, currentUserId);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_DELETE_FAILED);
        }
        deleteOssImages(imageUrls);
    }

    @Override
    @Transactional
    public void appendImages(Long goodsId, List<String> imageUrls) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        List<String> normalizedImageUrls = normalizeGoodsImageUrls(imageUrls);
        if (normalizedImageUrls.isEmpty()) {
            return;
        }

        List<String> mergedImageUrls = new ArrayList<>(getGoodsImageUrls(goodsId));
        for (String imageUrl : normalizedImageUrls) {
            if (!mergedImageUrls.contains(imageUrl)) {
                mergedImageUrls.add(imageUrl);
            }
        }
        if (mergedImageUrls.size() > MAX_GOODS_IMAGE_COUNT) {
            throw new BaseException(MessageConstant.GOODS_IMAGES_TOO_MANY);
        }

        replaceGoodsImages(goodsId, mergedImageUrls);
        if (!StringUtils.hasText(currentGoods.getCover()) && !mergedImageUrls.isEmpty()) {
            updateGoodsCover(currentGoods, mergedImageUrls.get(0));
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long goodsId, String imageUrl) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        String normalizedImageUrl = normalizeSingleGoodsImageUrl(imageUrl);
        int rows = goodsImageMapper.deleteByGoodsIdAndUrl(goodsId, normalizedImageUrl);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_IMAGE_URL_INVALID);
        }

        List<String> remainUrls = getGoodsImageUrls(goodsId);
        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .sellerId(currentGoods.getSellerId())
                .categoryId(currentGoods.getCategoryId())
                .title(currentGoods.getTitle())
                .detail(currentGoods.getDetail())
                .price(currentGoods.getPrice())
                .oldPrice(currentGoods.getOldPrice())
                .quality(currentGoods.getQuality())
                .location(currentGoods.getLocation())
                .cover(remainUrls.isEmpty() ? null : remainUrls.get(0))
                .status(currentGoods.getStatus())
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .viewCount(currentGoods.getViewCount())
                .favoriteCount(currentGoods.getFavoriteCount())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int updateRows = goodsMapper.updateById(updateGoods);
        if (updateRows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
        deleteOssImages(List.of(normalizedImageUrl));
    }

    @Override
    public PageResult page(SellerGoodsPageQueryDTO dto) {
        Long currentUserId = requireSellerUserId();
        SellerGoodsPageQueryDTO queryDTO = dto == null ? new SellerGoodsPageQueryDTO() : dto;
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<SellerGoodsPageVO> records = goodsMapper.pageSeller(currentUserId, queryDTO);
        Page<SellerGoodsPageVO> pageInfo = (Page<SellerGoodsPageVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public SellerGoodsDetailVO getDetail(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        SellerGoodsDetailVO detailVO = goodsMapper.detailSeller(goodsId, currentUserId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        detailVO.setImages(goodsImageMapper.selectByGoodsId(goodsId));
        return detailVO;
    }

    @Override
    @Transactional
    public void submit(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        if (currentGoods.getCategoryId() == null || currentGoods.getCategoryId() <= GoodsConstant.DEFAULT_CATEGORY_ID) {
            throw new BaseException(MessageConstant.GOODS_CATEGORY_REQUIRED);
        }
        Category category = categoryMapper.getById(currentGoods.getCategoryId());
        if (category == null || !StatusConstant.ENABLE.equals(category.getStatus())) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .status(GoodsConstant.STATUS_PENDING_AUDIT)
                .reason(null)
                .auditAdminId(null)
                .auditTime(null)
                .publishTime(null)
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_SUBMIT_AUDIT_FAILED);
        }
    }

    @Override
    @Transactional
    public void onShelf(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .status(GoodsConstant.STATUS_ON_SALE)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(LocalDateTime.now())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_ON_SHELF_FAILED);
        }
    }

    @Override
    @Transactional
    public void offShelf(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .status(GoodsConstant.STATUS_OFF_SHELF)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_FAILED);
        }
    }

    @Override
    @Transactional
    public void sold(Long goodsId) {
        Long currentUserId = requireSellerUserId();
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .status(GoodsConstant.STATUS_SOLD)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_MARK_SOLD_FAILED);
        }
    }

    private Long requireSellerUserId() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        return currentUserId;
    }

    private void validateCategory(Long categoryId) {
        if (categoryId != null && categoryId > GoodsConstant.DEFAULT_CATEGORY_ID) {
            Category category = categoryMapper.getById(categoryId);
            if (category == null || !StatusConstant.ENABLE.equals(category.getStatus())) {
                throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
            }
        }
    }

    private void replaceGoodsImages(Long goodsId, List<String> imageUrls) {
        List<String> oldImageUrls = getGoodsImageUrls(goodsId);
        goodsImageMapper.deleteByGoodsId(goodsId);
        if (imageUrls == null || imageUrls.isEmpty()) {
            deleteOssImages(oldImageUrls);
            return;
        }

        Long currentUserId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        List<GoodsImage> goodsImages = new ArrayList<>();
        int sort = 0;
        for (String imageUrl : imageUrls) {
            GoodsImage goodsImage = GoodsImage.builder()
                    .goodsId(goodsId)
                    .url(imageUrl)
                    .sort(sort)
                    .isCover(sort == 0 ? 1 : 0)
                    .createTime(now)
                    .updateTime(now)
                    .createUser(currentUserId)
                    .updateUser(currentUserId)
                    .build();
            goodsImages.add(goodsImage);
            sort++;
        }
        if (!goodsImages.isEmpty()) {
            goodsImageMapper.insertBatch(goodsImages);
        }

        List<String> staleImageUrls = oldImageUrls.stream()
                .filter(oldUrl -> !imageUrls.contains(oldUrl))
                .collect(Collectors.toList());
        deleteOssImages(staleImageUrls);
    }

    private List<String> normalizeGoodsImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        Set<String> normalizedSet = new LinkedHashSet<>();
        for (String imageUrl : imageUrls) {
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }
            normalizedSet.add(normalizeSingleGoodsImageUrl(imageUrl));
        }
        if (normalizedSet.size() > MAX_GOODS_IMAGE_COUNT) {
            throw new BaseException(MessageConstant.GOODS_IMAGES_TOO_MANY);
        }
        return new ArrayList<>(normalizedSet);
    }

    private String normalizeSingleGoodsImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new BaseException(MessageConstant.GOODS_IMAGE_URL_INVALID);
        }
        String normalized = imageUrl.trim();
        if (!ValidationRuleUtil.isAllowedImageUrl(normalized)) {
            throw new BaseException(MessageConstant.GOODS_IMAGE_URL_INVALID);
        }
        return normalized;
    }

    private List<String> getGoodsImageUrls(Long goodsId) {
        return goodsImageMapper.selectEntitiesByGoodsId(goodsId).stream()
                .map(GoodsImage::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private void updateGoodsCover(Goods currentGoods, String coverUrl) {
        Goods updateGoods = Goods.builder()
                .id(currentGoods.getId())
                .sellerId(currentGoods.getSellerId())
                .categoryId(currentGoods.getCategoryId())
                .title(currentGoods.getTitle())
                .detail(currentGoods.getDetail())
                .price(currentGoods.getPrice())
                .oldPrice(currentGoods.getOldPrice())
                .quality(currentGoods.getQuality())
                .location(currentGoods.getLocation())
                .cover(coverUrl)
                .status(currentGoods.getStatus())
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .viewCount(currentGoods.getViewCount())
                .favoriteCount(currentGoods.getFavoriteCount())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
    }

    private void deleteOssImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        try {
            aliOssUtil.deleteByUrls(imageUrls);
        } catch (Exception ex) {
            throw new BaseException(MessageConstant.FILE_DELETE_FAILED);
        }
    }
}
