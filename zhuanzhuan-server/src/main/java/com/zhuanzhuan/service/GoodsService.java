package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsDraftSaveDTO;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsStatUpdateDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.GoodsVO;

public interface GoodsService {

    Long createDraft(GoodsDraftSaveDTO goodsDraftSaveDTO);

    void updateDraft(Long id, GoodsDraftSaveDTO goodsDraftSaveDTO);

    void submitAudit(Long id);

    void onShelf(Long id);

    void offShelf(Long id);

    void markSold(Long id);

    PageResult pageBySeller(SellerGoodsPageQueryDTO pageQueryDTO);

    PageResult adminPage(AdminGoodsPageQueryDTO pageQueryDTO);

    PageResult pageForUser(GoodsPageQueryDTO pageQueryDTO);

    GoodsVO getDetail(Long id);

    void adminAudit(AdminGoodsAuditDTO adminGoodsAuditDTO);

    void updateStats(GoodsStatUpdateDTO goodsStatUpdateDTO);
}
