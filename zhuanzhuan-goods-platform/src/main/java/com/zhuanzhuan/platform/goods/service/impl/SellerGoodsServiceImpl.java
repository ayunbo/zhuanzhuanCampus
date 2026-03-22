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
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.SellerGoodsDetailVO;
import com.zhuanzhuan.vo.SellerGoodsPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 卖家端商品服务实现。
 */
@Service
public class SellerGoodsServiceImpl implements SellerGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建商品草稿。
     *
     * @param dto 商品保存对象
     * @return 商品 ID
     */
    @Override
    @Transactional
    public Long create(GoodsSaveDTO dto) {
        // 1. 校验卖家登录信息。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }

        // 2. 校验分类信息。
        GoodsSaveDTO saveDTO = dto == null ? new GoodsSaveDTO() : dto;
        if (saveDTO.getCategoryId() != null && saveDTO.getCategoryId() > GoodsConstant.DEFAULT_CATEGORY_ID) {
            Category category = categoryMapper.getById(saveDTO.getCategoryId());
            if (category == null || !StatusConstant.ENABLE.equals(category.getStatus())) {
                throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
            }
        }

        // 3. 保存商品草稿。
        Goods goods = Goods.builder()
                .id(IdGenerator.nextId())
                .sellerId(currentUserId)
                .categoryId(saveDTO.getCategoryId() == null ? GoodsConstant.DEFAULT_CATEGORY_ID : saveDTO.getCategoryId())
                .title(saveDTO.getTitle() == null ? "" : saveDTO.getTitle())
                .detail(saveDTO.getDetail())
                .price(saveDTO.getPrice() == null ? BigDecimal.ZERO : saveDTO.getPrice())
                .oldPrice(saveDTO.getOldPrice())
                .quality(saveDTO.getQuality() == null ? GoodsConstant.DEFAULT_QUALITY : saveDTO.getQuality())
                .location(saveDTO.getLocation())
                .status(GoodsConstant.STATUS_DRAFT)
                .cover(saveDTO.getImageUrls() == null || saveDTO.getImageUrls().isEmpty() ? null : saveDTO.getImageUrls().get(0))
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

        // 4. 保存商品图片。
        replaceGoodsImages(goods.getId(), saveDTO.getImageUrls());
        return goods.getId();
    }

    /**
     * 修改商品。
     *
     * @param goodsId 商品 ID
     * @param dto 商品保存对象
     */
    @Override
    @Transactional
    public void update(Long goodsId, GoodsSaveDTO dto) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 校验分类信息。
        GoodsSaveDTO saveDTO = dto == null ? new GoodsSaveDTO() : dto;
        if (saveDTO.getCategoryId() != null && saveDTO.getCategoryId() > GoodsConstant.DEFAULT_CATEGORY_ID) {
            Category category = categoryMapper.getById(saveDTO.getCategoryId());
            if (category == null || !StatusConstant.ENABLE.equals(category.getStatus())) {
                throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
            }
        }

        // 3. 组装更新数据。
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
                .cover(saveDTO.getImageUrls() == null || saveDTO.getImageUrls().isEmpty() ? null : saveDTO.getImageUrls().get(0))
                .status(targetStatus)
                .reason(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getReason())
                .auditAdminId(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getAuditAdminId())
                .auditTime(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getAuditTime())
                .publishTime(GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus()) ? null : currentGoods.getPublishTime())
                .version(currentGoods.getVersion())
                .build();

        // 4. 更新商品并替换图片。
        int rows = goodsMapper.updateById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
        replaceGoodsImages(goodsId, saveDTO.getImageUrls());
    }

    /**
     * 删除商品。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    public void delete(Long goodsId) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 删除商品图片。
        goodsImageMapper.deleteByGoodsId(goodsId);

        // 3. 删除商品主记录。
        int rows = goodsMapper.deleteByIdAndSellerId(goodsId, currentUserId);
        if (rows <= 0) {
            throw new BaseException("商品删除失败");
        }
    }

    /**
     * 分页查询卖家商品。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult page(SellerGoodsPageQueryDTO dto) {
        // 1. 校验卖家登录信息。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }

        // 2. 执行分页查询。
        SellerGoodsPageQueryDTO queryDTO = dto == null ? new SellerGoodsPageQueryDTO() : dto;
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<SellerGoodsPageVO> records = goodsMapper.pageSeller(currentUserId, queryDTO);
        Page<SellerGoodsPageVO> pageInfo = (Page<SellerGoodsPageVO>) records;

        // 3. 返回分页结果。
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询卖家商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @Override
    public SellerGoodsDetailVO getDetail(Long goodsId) {
        // 1. 校验卖家登录信息。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }

        // 2. 查询商品详情。
        SellerGoodsDetailVO detailVO = goodsMapper.detailSeller(goodsId, currentUserId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 3. 补充图片列表。
        detailVO.setImages(goodsImageMapper.selectByGoodsId(goodsId));
        return detailVO;
    }

    /**
     * 提交商品审核。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    public void submit(Long goodsId) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 校验分类信息。
        if (currentGoods.getCategoryId() == null || currentGoods.getCategoryId() <= GoodsConstant.DEFAULT_CATEGORY_ID) {
            throw new BaseException(MessageConstant.GOODS_CATEGORY_REQUIRED);
        }
        Category category = categoryMapper.getById(currentGoods.getCategoryId());
        if (category == null || !StatusConstant.ENABLE.equals(category.getStatus())) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 3. 更新商品状态。
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

    /**
     * 重新上架商品。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    public void onShelf(Long goodsId) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 更新商品状态。
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

    /**
     * 下架商品。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    public void offShelf(Long goodsId) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 更新商品状态。
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

    /**
     * 标记商品已售出。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    public void sold(Long goodsId) {
        // 1. 校验卖家登录信息和商品归属。
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        User currentUser = userMapper.getById(currentUserId);
        if (currentUser == null || !RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        Goods currentGoods = goodsMapper.selectByIdAndSellerId(goodsId, currentUserId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 2. 更新商品状态。
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

    /**
     * 替换商品图片列表。
     *
     * @param goodsId 商品 ID
     * @param imageUrls 图片地址列表
     */
    private void replaceGoodsImages(Long goodsId, List<String> imageUrls) {
        goodsImageMapper.deleteByGoodsId(goodsId);
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        Long currentUserId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        List<GoodsImage> goodsImages = new ArrayList<>();
        int sort = 0;
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.isEmpty()) {
                continue;
            }
            GoodsImage goodsImage = GoodsImage.builder()
                    .id(IdGenerator.nextId())
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
    }
}
