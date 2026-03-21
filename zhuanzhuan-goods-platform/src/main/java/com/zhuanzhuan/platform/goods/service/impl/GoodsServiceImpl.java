package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsDraftSaveDTO;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsStatUpdateDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.goods.service.GoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.utils.ValidationRuleUtil;
import com.zhuanzhuan.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品业务实现。
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 新增商品草稿。
     */
    @Override
    public Long createDraft(GoodsDraftSaveDTO goodsDraftSaveDTO) {
        // 1、校验卖家身份和新增参数
        User seller = assertCurrentSeller();
        if (goodsDraftSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、构造草稿实体
        Goods draft = buildDraftEntity(goodsDraftSaveDTO, null);
        draft.setId(IdGenerator.nextId());
        draft.setSellerId(seller.getId());
        draft.setStatus(GoodsConstant.STATUS_DRAFT);
        draft.setViewCount(GoodsConstant.INITIAL_VIEW_COUNT);
        draft.setFavoriteCount(GoodsConstant.INITIAL_FAVORITE_COUNT);
        draft.setVersion(GoodsConstant.INITIAL_VERSION);

        // 3、保存草稿
        int rows = goodsMapper.insert(draft);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_CREATE_FAILED);
        }
        return draft.getId();
    }

    /**
     * 修改商品草稿。
     */
    @Override
    public void updateDraft(Long id, GoodsDraftSaveDTO goodsDraftSaveDTO) {
        // 1、校验参数
        if (id == null || goodsDraftSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询卖家自己的商品
        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!isEditableStatus(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_EDIT_FORBIDDEN);
        }

        // 3、构造更新实体并保存
        Goods updateEntity = buildDraftEntity(goodsDraftSaveDTO, currentGoods);
        updateEntity.setId(currentGoods.getId());
        updateEntity.setSellerId(currentGoods.getSellerId());
        updateEntity.setVersion(currentGoods.getVersion());

        int rows = goodsMapper.updateDraftById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
    }

    /**
     * 提交商品审核。
     */
    @Override
    @Transactional
    public void submitAudit(Long id) {
        // 1、校验参数并查询商品
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());

        // 2、只有草稿和驳回状态允许再次提审，其他状态说明商品已经进入别的流程
        if (!GoodsConstant.STATUS_DRAFT.equals(currentGoods.getStatus())
                && !GoodsConstant.STATUS_REJECTED.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_SUBMIT_AUDIT_STATUS_INVALID);
        }

        // 3、提审前必须补齐分类、标题、价格、封面等关键字段
        validateRequiredForAudit(currentGoods);

        // 4、清空上一轮审核痕迹，重新把商品推进到待审核状态
        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(GoodsConstant.STATUS_PENDING_AUDIT)
                .reason(null)
                .auditAdminId(null)
                .auditTime(null)
                .publishTime(null)
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_SUBMIT_AUDIT_FAILED);
        }
    }

    /**
     * 上架商品。
     */
    @Override
    @Transactional
    public void onShelf(Long id) {
        // 1、校验参数并查询商品
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_ON_SHELF_STATUS_INVALID);
        }

        // 2、恢复上架时保留历史审核信息，只更新当前生命周期状态
        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(GoodsConstant.STATUS_ON_SALE)
                .reason(null)
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(LocalDateTime.now())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_ON_SHELF_FAILED);
        }
    }

    /**
     * 下架商品。
     */
    @Override
    @Transactional
    public void offShelf(Long id) {
        // 1、校验参数并查询商品
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!GoodsConstant.STATUS_ON_SALE.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_STATUS_INVALID);
        }

        // 2、下架只改变生命周期状态，审核信息和发布时间继续保留
        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(GoodsConstant.STATUS_OFF_SHELF)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_FAILED);
        }
    }

    /**
     * 标记商品已售。
     */
    @Override
    @Transactional
    public void markSold(Long id) {
        // 1、校验参数并查询商品
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!GoodsConstant.STATUS_ON_SALE.equals(currentGoods.getStatus())
                && !GoodsConstant.STATUS_LOCKED.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_MARK_SOLD_STATUS_INVALID);
        }

        // 2、成交后把商品改成已售，保留之前审核和上架期间的关键信息
        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(GoodsConstant.STATUS_SOLD)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_MARK_SOLD_FAILED);
        }
    }

    /**
     * 卖家分页查询商品。
     */
    @Override
    public PageResult pageBySeller(SellerGoodsPageQueryDTO pageQueryDTO) {
        // 1、校验卖家身份并整理分页参数
        User seller = assertCurrentSeller();
        SellerGoodsPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new SellerGoodsPageQueryDTO();
        }

        int page = normalizePage(queryDTO.getPage());
        int pageSize = normalizePageSize(queryDTO.getPageSize());

        // 2、校验筛选条件
        if (queryDTO.getStatus() != null && !isValidGoodsStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<GoodsVO> records = goodsMapper.pageBySeller(seller.getId(), queryDTO.getStatus());
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 管理端分页查询商品。
     */
    @Override
    public PageResult adminPage(AdminGoodsPageQueryDTO pageQueryDTO) {
        // 1、校验管理员登录状态并整理分页参数
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        AdminGoodsPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new AdminGoodsPageQueryDTO();
        }

        int page = normalizePage(queryDTO.getPage());
        int pageSize = normalizePageSize(queryDTO.getPageSize());

        // 2、整理并校验查询条件
        String title = queryDTO.getTitle();
        if (StringUtils.hasText(title)) {
            queryDTO.setTitle(title.trim());
        } else {
            queryDTO.setTitle(null);
        }

        if (queryDTO.getStatus() != null && !isValidGoodsStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<GoodsVO> records = goodsMapper.adminPage(queryDTO);
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 用户分页查询商品。
     */
    @Override
    public PageResult pageForUser(GoodsPageQueryDTO pageQueryDTO) {
        // 1、整理分页参数
        GoodsPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new GoodsPageQueryDTO();
        }

        int page = normalizePage(queryDTO.getPage());
        int pageSize = normalizePageSize(queryDTO.getPageSize());

        // 2、整理查询标题
        String title = queryDTO.getTitle();
        if (StringUtils.hasText(title)) {
            queryDTO.setTitle(title.trim());
        } else {
            queryDTO.setTitle(null);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<GoodsVO> records = goodsMapper.pageForUser(queryDTO);
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询商品详情。
     */
    @Override
    public GoodsVO getDetail(Long id) {
        // 1、校验查询参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询并校验商品可见性
        GoodsVO goodsVO = goodsMapper.getDetailById(id);
        if (goodsVO == null || !isVisibleForUser(goodsVO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 3、补充状态文案
        goodsVO.setStatusDesc(toStatusDesc(goodsVO.getStatus()));
        return goodsVO;
    }

    /**
     * 审核商品。
     */
    @Override
    @Transactional
    public void adminAudit(AdminGoodsAuditDTO adminGoodsAuditDTO) {
        // 1、校验审核参数
        if (adminGoodsAuditDTO == null
                || adminGoodsAuditDTO.getGoodsId() == null
                || adminGoodsAuditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        Integer targetStatus = adminGoodsAuditDTO.getStatus();
        if (!GoodsConstant.STATUS_ON_SALE.equals(targetStatus)
                && !GoodsConstant.STATUS_REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_INVALID);
        }

        // 2、获取管理员并查询商品
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        Goods currentGoods = goodsMapper.getById(adminGoodsAuditDTO.getGoodsId());
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        if (!GoodsConstant.STATUS_PENDING_AUDIT.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_FLOW_INVALID);
        }

        // 3、审核通过会直接让商品进入在售，因此先确认卖家账号仍是正常状态
        if (GoodsConstant.STATUS_ON_SALE.equals(targetStatus)) {
            User seller = userMapper.getById(currentGoods.getSellerId());
            if (seller == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!UserStatusConstant.NORMAL.equals(seller.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        // 4、整理驳回原因并校验，驳回时必须给卖家明确的失败原因
        String rejectReason = adminGoodsAuditDTO.getReason();
        if (!StringUtils.hasText(rejectReason)) {
            rejectReason = null;
        }

        if (GoodsConstant.STATUS_REJECTED.equals(targetStatus) && !StringUtils.hasText(rejectReason)) {
            throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
        }
        if (rejectReason != null && rejectReason.length() > GoodsConstant.MAX_AUDIT_REASON_LENGTH) {
            throw new BaseException(MessageConstant.REJECT_REASON_TOO_LONG);
        }

        // 5、更新审核结果，通过时写入发布时间，驳回时写入驳回原因
        LocalDateTime auditTime = LocalDateTime.now();
        LocalDateTime publishTime = null;
        if (GoodsConstant.STATUS_ON_SALE.equals(targetStatus)) {
            publishTime = auditTime;
        }

        String auditReason = null;
        if (GoodsConstant.STATUS_REJECTED.equals(targetStatus)) {
            auditReason = rejectReason;
        }

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(targetStatus)
                .reason(auditReason)
                .auditAdminId(adminId)
                .auditTime(auditTime)
                .publishTime(publishTime)
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_FAILED);
        }
    }

    /**
     * 更新商品统计。
     */
    @Override
    public void updateStats(GoodsStatUpdateDTO goodsStatUpdateDTO) {
        // 1、校验统计参数
        if (goodsStatUpdateDTO == null || goodsStatUpdateDTO.getGoodsId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理增量数据，未传的增量按 0 处理，避免把空值直接写入统计逻辑
        int viewDelta = GoodsConstant.INITIAL_VIEW_COUNT;
        if (goodsStatUpdateDTO.getViewDelta() != null) {
            viewDelta = goodsStatUpdateDTO.getViewDelta();
        }

        int favoriteDelta = GoodsConstant.INITIAL_FAVORITE_COUNT;
        if (goodsStatUpdateDTO.getFavoriteDelta() != null) {
            favoriteDelta = goodsStatUpdateDTO.getFavoriteDelta();
        }

        if (viewDelta == GoodsConstant.INITIAL_VIEW_COUNT
                && favoriteDelta == GoodsConstant.INITIAL_FAVORITE_COUNT) {
            throw new BaseException(MessageConstant.GOODS_STATS_UPDATE_EMPTY);
        }

        // 3、校验商品存在并更新统计
        Goods currentGoods = goodsMapper.getById(goodsStatUpdateDTO.getGoodsId());
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        int rows = goodsMapper.updateStats(goodsStatUpdateDTO.getGoodsId(), viewDelta, favoriteDelta);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_STATS_UPDATE_FAILED);
        }
    }

    /**
     * 构建商品草稿实体。
     */
    private Goods buildDraftEntity(GoodsDraftSaveDTO dto, Goods currentGoods) {
        // 1、先读取当前草稿默认值，编辑场景下优先沿用数据库中的旧值
        Long categoryId = GoodsConstant.DEFAULT_CATEGORY_ID;
        String title = "";
        String detail = null;
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal oldPrice = null;
        Integer quality = GoodsConstant.DEFAULT_QUALITY;
        String location = null;
        String cover = null;

        if (currentGoods != null) {
            if (currentGoods.getCategoryId() != null) {
                categoryId = currentGoods.getCategoryId();
            }
            if (currentGoods.getTitle() != null) {
                title = currentGoods.getTitle();
            }
            detail = currentGoods.getDetail();
            if (currentGoods.getPrice() != null) {
                price = currentGoods.getPrice();
            }
            oldPrice = currentGoods.getOldPrice();
            if (currentGoods.getQuality() != null) {
                quality = currentGoods.getQuality();
            }
            location = currentGoods.getLocation();
            cover = currentGoods.getCover();
        }

        // 2、再用本次请求覆盖已传字段，没传的字段继续保留旧值
        if (dto.getCategoryId() != null) {
            categoryId = dto.getCategoryId();
        }

        if (dto.getTitle() != null) {
            title = dto.getTitle().trim();
            if (!StringUtils.hasText(title)) {
                title = "";
            }
        }

        if (dto.getDetail() != null) {
            if (StringUtils.hasText(dto.getDetail())) {
                detail = dto.getDetail();
            } else {
                detail = null;
            }
        }

        if (dto.getPrice() != null) {
            price = dto.getPrice();
        }

        if (dto.getOldPrice() != null) {
            oldPrice = dto.getOldPrice();
        }

        if (dto.getQuality() != null) {
            quality = dto.getQuality();
        }

        if (dto.getLocation() != null) {
            if (StringUtils.hasText(dto.getLocation())) {
                location = dto.getLocation();
            } else {
                location = null;
            }
        }

        if (dto.getCover() != null) {
            if (StringUtils.hasText(dto.getCover())) {
                cover = dto.getCover().trim();
            } else {
                cover = null;
            }
        }

        // 3、统一校验草稿字段，避免非法数据在草稿阶段就落库
        validateDraftFields(categoryId, title, price, oldPrice, quality, location, cover);

        return Goods.builder()
                .categoryId(categoryId)
                .title(title)
                .detail(detail)
                .price(price)
                .oldPrice(oldPrice)
                .quality(quality)
                .location(location)
                .cover(cover)
                .build();
    }

    /**
     * 校验草稿字段是否合法。
     */
    private void validateDraftFields(Long categoryId,
                                     String title,
                                     BigDecimal price,
                                     BigDecimal oldPrice,
                                     Integer quality,
                                     String location,
                                     String cover) {
        // 1、校验分类和标题基础规则
        if (categoryId == null || categoryId < 0) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }
        if (title != null && title.length() > GoodsConstant.MAX_TITLE_LENGTH) {
            throw new BaseException(MessageConstant.GOODS_TITLE_TOO_LONG);
        }

        // 2、校验价格关系，原价不能为负数，也不能低于现价
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(MessageConstant.GOODS_PRICE_INVALID);
        }
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(MessageConstant.GOODS_OLD_PRICE_INVALID);
        }
        if (oldPrice != null
                && price.compareTo(BigDecimal.ZERO) > 0
                && oldPrice.compareTo(price) < 0) {
            throw new BaseException(MessageConstant.GOODS_OLD_PRICE_INVALID);
        }

        // 3、校验成色、地点和封面地址，保证前台展示字段都在可控范围内
        if (quality == null
                || quality < GoodsConstant.MIN_QUALITY
                || quality > GoodsConstant.MAX_QUALITY) {
            throw new BaseException(MessageConstant.GOODS_QUALITY_INVALID);
        }
        if (location != null && location.length() > GoodsConstant.MAX_LOCATION_LENGTH) {
            throw new BaseException(MessageConstant.GOODS_LOCATION_TOO_LONG);
        }
        if (cover != null && !ValidationRuleUtil.isValidHttpUrl(cover)) {
            throw new BaseException(MessageConstant.GOODS_COVER_INVALID);
        }
    }

    /**
     * 校验商品提审所需的关键字段。
     */
    private void validateRequiredForAudit(Goods goods) {
        // 1、先复用草稿规则，确保基础格式和长度限制都合法
        validateDraftFields(
                goods.getCategoryId(),
                goods.getTitle(),
                goods.getPrice(),
                goods.getOldPrice(),
                goods.getQuality(),
                goods.getLocation(),
                goods.getCover()
        );

        // 2、提审阶段再补充关键必填校验，避免空标题、空分类、空价格进入审核队列
        if (goods.getCategoryId() == null || goods.getCategoryId() <= GoodsConstant.DEFAULT_CATEGORY_ID) {
            throw new BaseException(MessageConstant.GOODS_CATEGORY_REQUIRED);
        }
        if (!StringUtils.hasText(goods.getTitle())) {
            throw new BaseException(MessageConstant.GOODS_TITLE_REQUIRED);
        }
        if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(MessageConstant.GOODS_PRICE_INVALID);
        }
    }

    /**
     * 校验当前登录用户必须是正常状态的卖家。
     */
    private User assertCurrentSeller() {
        // 1、先校验登录态
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、再校验用户存在且账号未被封禁
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!UserStatusConstant.NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、商品管理流程只对卖家开放
        if (!RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        return currentUser;
    }

    /**
     * 查询卖家自己的商品并校验归属。
     */
    private Goods getSellerOwnedGoods(Long goodsId, Long sellerId) {
        // 1、按商品 ID 和卖家 ID 联合查询，防止用户越权操作他人的商品
        Goods goods = goodsMapper.getByIdAndSellerId(goodsId, sellerId);
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        return goods;
    }

    /**
     * 补充商品状态文案。
     */
    private void fillStatusDesc(List<GoodsVO> records) {
        // 1、分页结果里通常只有状态码，这里统一补全给前端展示的状态文案
        for (GoodsVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }
    }

    /**
     * 校验商品当前状态是否允许编辑。
     */
    private boolean isEditableStatus(Integer status) {
        return GoodsConstant.STATUS_DRAFT.equals(status)
                || GoodsConstant.STATUS_REJECTED.equals(status)
                || GoodsConstant.STATUS_OFF_SHELF.equals(status);
    }

    /**
     * 校验商品当前状态是否允许前台查看。
     */
    private boolean isVisibleForUser(Integer status) {
        return GoodsConstant.STATUS_ON_SALE.equals(status)
                || GoodsConstant.STATUS_LOCKED.equals(status)
                || GoodsConstant.STATUS_SOLD.equals(status);
    }

    /**
     * 校验商品状态枚举是否合法。
     */
    private boolean isValidGoodsStatus(Integer status) {
        return GoodsConstant.STATUS_DRAFT.equals(status)
                || GoodsConstant.STATUS_PENDING_AUDIT.equals(status)
                || GoodsConstant.STATUS_REJECTED.equals(status)
                || GoodsConstant.STATUS_ON_SALE.equals(status)
                || GoodsConstant.STATUS_LOCKED.equals(status)
                || GoodsConstant.STATUS_SOLD.equals(status)
                || GoodsConstant.STATUS_OFF_SHELF.equals(status);
    }

    /**
     * 处理分页页码默认值。
     */
    private int normalizePage(Integer page) {
        if (page == null || page < PageConstant.DEFAULT_PAGE) {
            return PageConstant.DEFAULT_PAGE;
        }
        return page;
    }

    /**
     * 处理分页大小默认值。
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < PageConstant.DEFAULT_PAGE) {
            return PageConstant.DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 转换商品状态文案。
     */
    private String toStatusDesc(Integer status) {
        if (GoodsConstant.STATUS_DRAFT.equals(status)) {
            return GoodsConstant.STATUS_DESC_DRAFT;
        }
        if (GoodsConstant.STATUS_PENDING_AUDIT.equals(status)) {
            return GoodsConstant.STATUS_DESC_PENDING_AUDIT;
        }
        if (GoodsConstant.STATUS_REJECTED.equals(status)) {
            return GoodsConstant.STATUS_DESC_REJECTED;
        }
        if (GoodsConstant.STATUS_ON_SALE.equals(status)) {
            return GoodsConstant.STATUS_DESC_ON_SALE;
        }
        if (GoodsConstant.STATUS_LOCKED.equals(status)) {
            return GoodsConstant.STATUS_DESC_LOCKED;
        }
        if (GoodsConstant.STATUS_SOLD.equals(status)) {
            return GoodsConstant.STATUS_DESC_SOLD;
        }
        if (GoodsConstant.STATUS_OFF_SHELF.equals(status)) {
            return GoodsConstant.STATUS_DESC_OFF_SHELF;
        }
        return GoodsConstant.STATUS_DESC_UNKNOWN;
    }
}
