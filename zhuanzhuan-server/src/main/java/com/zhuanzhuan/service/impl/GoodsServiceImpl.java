package com.zhuanzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
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
import com.zhuanzhuan.mapper.GoodsMapper;
import com.zhuanzhuan.mapper.UserMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.GoodsService;
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

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Long createDraft(GoodsDraftSaveDTO goodsDraftSaveDTO) {
        User seller = assertCurrentSeller();
        if (goodsDraftSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Goods draft = buildDraftEntity(goodsDraftSaveDTO, null);
        draft.setId(IdGenerator.nextId());
        draft.setSellerId(seller.getId());
        draft.setStatus(Goods.STATUS_DRAFT);
        draft.setViewCount(0);
        draft.setFavoriteCount(0);
        draft.setVersion(0);

        int rows = goodsMapper.insert(draft);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_CREATE_FAILED);
        }
        return draft.getId();
    }

    @Override
    public void updateDraft(Long id, GoodsDraftSaveDTO goodsDraftSaveDTO) {
        if (id == null || goodsDraftSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!isEditableStatus(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_EDIT_FORBIDDEN);
        }

        Goods updateEntity = buildDraftEntity(goodsDraftSaveDTO, currentGoods);
        updateEntity.setId(currentGoods.getId());
        updateEntity.setSellerId(currentGoods.getSellerId());
        updateEntity.setVersion(currentGoods.getVersion());

        int rows = goodsMapper.updateDraftById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional
    public void submitAudit(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!Goods.STATUS_DRAFT.equals(currentGoods.getStatus())
                && !Goods.STATUS_REJECTED.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_SUBMIT_AUDIT_STATUS_INVALID);
        }

        validateRequiredForAudit(currentGoods);

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(Goods.STATUS_PENDING_AUDIT)
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

    @Override
    @Transactional
    public void onShelf(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!Goods.STATUS_OFF_SHELF.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_ON_SHELF_STATUS_INVALID);
        }

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(Goods.STATUS_ON_SALE)
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

    @Override
    @Transactional
    public void offShelf(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!Goods.STATUS_ON_SALE.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_STATUS_INVALID);
        }

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(Goods.STATUS_OFF_SHELF)
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

    @Override
    @Transactional
    public void markSold(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        User seller = assertCurrentSeller();
        Goods currentGoods = getSellerOwnedGoods(id, seller.getId());
        if (!Goods.STATUS_ON_SALE.equals(currentGoods.getStatus())
                && !Goods.STATUS_LOCKED.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_MARK_SOLD_STATUS_INVALID);
        }

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(Goods.STATUS_SOLD)
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

    @Override
    public PageResult pageBySeller(SellerGoodsPageQueryDTO pageQueryDTO) {
        User seller = assertCurrentSeller();
        SellerGoodsPageQueryDTO queryDTO = pageQueryDTO == null ? new SellerGoodsPageQueryDTO() : pageQueryDTO;
        int page = normalizePage(queryDTO.getPage());
        int pageSize = normalizePageSize(queryDTO.getPageSize());

        if (queryDTO.getStatus() != null && !isValidGoodsStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }

        PageHelper.startPage(page, pageSize);
        List<GoodsVO> records = goodsMapper.pageBySeller(seller.getId(), queryDTO.getStatus());
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public PageResult adminPage(AdminGoodsPageQueryDTO pageQueryDTO) {
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        AdminGoodsPageQueryDTO queryDTO = pageQueryDTO == null ? new AdminGoodsPageQueryDTO() : pageQueryDTO;
        int page = normalizePage(queryDTO.getPage());
        int pageSize = normalizePageSize(queryDTO.getPageSize());

        if (queryDTO.getStatus() != null && !isValidGoodsStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }

        queryDTO.setTitle(trimToNull(queryDTO.getTitle()));

        PageHelper.startPage(page, pageSize);
        List<GoodsVO> records = goodsMapper.adminPage(queryDTO);
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public PageResult pageForUser(GoodsPageQueryDTO pageQueryDTO) {
        GoodsPageQueryDTO queryDTO = pageQueryDTO == null ? new GoodsPageQueryDTO() : pageQueryDTO;
        queryDTO.setTitle(trimToNull(queryDTO.getTitle()));

        PageHelper.startPage(normalizePage(queryDTO.getPage()), normalizePageSize(queryDTO.getPageSize()));
        List<GoodsVO> records = goodsMapper.pageForUser(queryDTO);
        Page<GoodsVO> pageInfo = (Page<GoodsVO>) records;
        fillStatusDesc(records);
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public GoodsVO getDetail(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        GoodsVO goodsVO = goodsMapper.getDetailById(id);
        if (goodsVO == null || !isVisibleForUser(goodsVO.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        goodsVO.setStatusDesc(toStatusDesc(goodsVO.getStatus()));
        return goodsVO;
    }

    @Override
    @Transactional
    public void adminAudit(AdminGoodsAuditDTO adminGoodsAuditDTO) {
        if (adminGoodsAuditDTO == null
                || adminGoodsAuditDTO.getGoodsId() == null
                || adminGoodsAuditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        Integer targetStatus = adminGoodsAuditDTO.getStatus();
        if (!Goods.STATUS_ON_SALE.equals(targetStatus) && !Goods.STATUS_REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_INVALID);
        }

        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        Goods currentGoods = goodsMapper.getById(adminGoodsAuditDTO.getGoodsId());
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        if (!Goods.STATUS_PENDING_AUDIT.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_FLOW_INVALID);
        }
        if (Goods.STATUS_ON_SALE.equals(targetStatus)) {
            User seller = userMapper.getById(currentGoods.getSellerId());
            if (seller == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!User.STATUS_NORMAL.equals(seller.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        String rejectReason = trimToNull(adminGoodsAuditDTO.getReason());
        if (Goods.STATUS_REJECTED.equals(targetStatus) && !StringUtils.hasText(rejectReason)) {
            throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
        }
        if (rejectReason != null && rejectReason.length() > 255) {
            throw new BaseException(MessageConstant.REJECT_REASON_TOO_LONG);
        }

        Goods updateEntity = Goods.builder()
                .id(currentGoods.getId())
                .status(targetStatus)
                .reason(Goods.STATUS_REJECTED.equals(targetStatus) ? rejectReason : null)
                .auditAdminId(adminId)
                .auditTime(LocalDateTime.now())
                .publishTime(Goods.STATUS_ON_SALE.equals(targetStatus) ? LocalDateTime.now() : null)
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();

        int rows = goodsMapper.updateLifecycleById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_FAILED);
        }
    }

    @Override
    public void updateStats(GoodsStatUpdateDTO goodsStatUpdateDTO) {
        if (goodsStatUpdateDTO == null || goodsStatUpdateDTO.getGoodsId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        int viewDelta = goodsStatUpdateDTO.getViewDelta() == null ? 0 : goodsStatUpdateDTO.getViewDelta();
        int favoriteDelta = goodsStatUpdateDTO.getFavoriteDelta() == null ? 0 : goodsStatUpdateDTO.getFavoriteDelta();
        if (viewDelta == 0 && favoriteDelta == 0) {
            throw new BaseException(MessageConstant.GOODS_STATS_UPDATE_EMPTY);
        }

        Goods currentGoods = goodsMapper.getById(goodsStatUpdateDTO.getGoodsId());
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        int rows = goodsMapper.updateStats(goodsStatUpdateDTO.getGoodsId(), viewDelta, favoriteDelta);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_STATS_UPDATE_FAILED);
        }
    }

    private Goods buildDraftEntity(GoodsDraftSaveDTO dto, Goods currentGoods) {
        Long categoryId = dto.getCategoryId() != null ? dto.getCategoryId()
                : currentGoods == null || currentGoods.getCategoryId() == null ? 0L : currentGoods.getCategoryId();
        String title = resolveDraftTitle(dto, currentGoods);
        String detail = resolveOptionalText(dto.getDetail(), currentGoods == null ? null : currentGoods.getDetail());
        BigDecimal price = dto.getPrice() != null ? dto.getPrice()
                : currentGoods == null || currentGoods.getPrice() == null ? BigDecimal.ZERO : currentGoods.getPrice();
        BigDecimal oldPrice = dto.getOldPrice() != null ? dto.getOldPrice()
                : currentGoods == null ? null : currentGoods.getOldPrice();
        Integer quality = dto.getQuality() != null ? dto.getQuality()
                : currentGoods == null ? 5 : currentGoods.getQuality();
        String location = resolveOptionalText(dto.getLocation(), currentGoods == null ? null : currentGoods.getLocation());
        String cover = resolveOptionalText(dto.getCover(), currentGoods == null ? null : currentGoods.getCover());

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

    private void validateDraftFields(Long categoryId,
                                     String title,
                                     BigDecimal price,
                                     BigDecimal oldPrice,
                                     Integer quality,
                                     String location,
                                     String cover) {
        if (categoryId == null || categoryId < 0) {
            throw new BaseException(MessageConstant.GOODS_STATUS_INVALID);
        }
        if (title != null && title.length() > 100) {
            throw new BaseException(MessageConstant.GOODS_TITLE_TOO_LONG);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(MessageConstant.GOODS_PRICE_INVALID);
        }
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(MessageConstant.GOODS_OLD_PRICE_INVALID);
        }
        if (oldPrice != null && price.compareTo(BigDecimal.ZERO) > 0 && oldPrice.compareTo(price) < 0) {
            throw new BaseException(MessageConstant.GOODS_OLD_PRICE_INVALID);
        }
        if (quality == null || quality < 1 || quality > 5) {
            throw new BaseException(MessageConstant.GOODS_QUALITY_INVALID);
        }
        if (location != null && location.length() > 120) {
            throw new BaseException(MessageConstant.GOODS_LOCATION_TOO_LONG);
        }
        if (cover != null && !ValidationRuleUtil.isValidHttpUrl(cover)) {
            throw new BaseException(MessageConstant.GOODS_COVER_INVALID);
        }
    }

    private void validateRequiredForAudit(Goods goods) {
        validateDraftFields(goods.getCategoryId(), goods.getTitle(), goods.getPrice(), goods.getOldPrice(),
                goods.getQuality(), goods.getLocation(), goods.getCover());

        if (goods.getCategoryId() == null || goods.getCategoryId() <= 0) {
            throw new BaseException(MessageConstant.GOODS_CATEGORY_REQUIRED);
        }
        if (!StringUtils.hasText(goods.getTitle())) {
            throw new BaseException(MessageConstant.GOODS_TITLE_REQUIRED);
        }
        if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(MessageConstant.GOODS_PRICE_INVALID);
        }
    }

    private User assertCurrentSeller() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!User.STATUS_NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }
        if (!User.ROLE_SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
        return currentUser;
    }

    private Goods getSellerOwnedGoods(Long goodsId, Long sellerId) {
        Goods goods = goodsMapper.getByIdAndSellerId(goodsId, sellerId);
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        return goods;
    }

    private void fillStatusDesc(List<GoodsVO> records) {
        for (GoodsVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }
    }

    private boolean isEditableStatus(Integer status) {
        return Goods.STATUS_DRAFT.equals(status)
                || Goods.STATUS_REJECTED.equals(status)
                || Goods.STATUS_OFF_SHELF.equals(status);
    }

    private boolean isVisibleForUser(Integer status) {
        return Goods.STATUS_ON_SALE.equals(status)
                || Goods.STATUS_LOCKED.equals(status)
                || Goods.STATUS_SOLD.equals(status);
    }

    private boolean isValidGoodsStatus(Integer status) {
        return Goods.STATUS_DRAFT.equals(status)
                || Goods.STATUS_PENDING_AUDIT.equals(status)
                || Goods.STATUS_REJECTED.equals(status)
                || Goods.STATUS_ON_SALE.equals(status)
                || Goods.STATUS_LOCKED.equals(status)
                || Goods.STATUS_SOLD.equals(status)
                || Goods.STATUS_OFF_SHELF.equals(status);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private String resolveDraftTitle(GoodsDraftSaveDTO dto, Goods currentGoods) {
        if (dto.getTitle() == null) {
            return currentGoods == null || currentGoods.getTitle() == null ? "" : currentGoods.getTitle();
        }
        String title = dto.getTitle().trim();
        return StringUtils.hasText(title) ? title : "";
    }

    private String resolveOptionalText(String incomingValue, String currentValue) {
        if (incomingValue == null) {
            return currentValue;
        }
        return trimToNull(incomingValue);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String toStatusDesc(Integer status) {
        if (Goods.STATUS_DRAFT.equals(status)) {
            return "草稿";
        }
        if (Goods.STATUS_PENDING_AUDIT.equals(status)) {
            return "待审核";
        }
        if (Goods.STATUS_REJECTED.equals(status)) {
            return "已驳回";
        }
        if (Goods.STATUS_ON_SALE.equals(status)) {
            return "在售";
        }
        if (Goods.STATUS_LOCKED.equals(status)) {
            return "锁定";
        }
        if (Goods.STATUS_SOLD.equals(status)) {
            return "已售出";
        }
        if (Goods.STATUS_OFF_SHELF.equals(status)) {
            return "已下架";
        }
        return "未知状态";
    }
}
