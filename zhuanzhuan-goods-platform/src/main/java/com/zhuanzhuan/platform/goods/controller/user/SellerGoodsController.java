package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.dto.GoodsDraftSaveDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/seller/goods")
public class SellerGoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/draft")
    public Result<Long> createDraft(@RequestBody GoodsDraftSaveDTO dto) {
        return Result.success(goodsService.createDraft(dto));
    }

    @PutMapping("/draft/{id}")
    public Result<Void> updateDraft(@PathVariable Long id, @RequestBody GoodsDraftSaveDTO dto) {
        goodsService.updateDraft(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/submit-audit")
    public Result<Void> submitAudit(@PathVariable Long id) {
        goodsService.submitAudit(id);
        return Result.success();
    }

    @PutMapping("/{id}/on-shelf")
    public Result<Void> onShelf(@PathVariable Long id) {
        goodsService.onShelf(id);
        return Result.success();
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        goodsService.offShelf(id);
        return Result.success();
    }

    @PutMapping("/{id}/sold")
    public Result<Void> sold(@PathVariable Long id) {
        goodsService.markSold(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SellerGoodsPageQueryDTO dto) {
        return Result.success(goodsService.pageBySeller(dto));
    }
}
