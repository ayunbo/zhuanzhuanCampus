package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.dto.GoodsSaveDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.platform.goods.service.SellerGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.SellerGoodsDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 卖家端商品控制器。
 */
@RestController
@RequestMapping("/user/seller/goods")
public class SellerGoodsController {

    @Autowired
    private SellerGoodsService sellerGoodsService;

    /**
     * 卖家创建商品草稿。
     *
     * @param dto 商品保存对象
     * @return 商品 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody GoodsSaveDTO dto) {
        return Result.success(sellerGoodsService.create(dto));
    }

    /**
     * 卖家修改商品。
     *
     * @param id 商品 ID
     * @param dto 商品保存对象
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}")
    public Result<Void> update(@PathVariable Long id, @RequestBody GoodsSaveDTO dto) {
        sellerGoodsService.update(id, dto);
        return Result.success();
    }

    /**
     * 卖家删除商品。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        sellerGoodsService.delete(id);
        return Result.success();
    }

    /**
     * 卖家分页查询自己的商品。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping({"", "/page"})
    public Result<PageResult> page(SellerGoodsPageQueryDTO dto) {
        return Result.success(sellerGoodsService.page(dto));
    }

    /**
     * 卖家查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<SellerGoodsDetailVO> detail(@PathVariable Long id) {
        return Result.success(sellerGoodsService.getDetail(id));
    }

    /**
     * 卖家提交商品审核。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        sellerGoodsService.submit(id);
        return Result.success();
    }

    /**
     * 卖家重新上架商品。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/on-shelf")
    public Result<Void> onShelf(@PathVariable Long id) {
        sellerGoodsService.onShelf(id);
        return Result.success();
    }

    /**
     * 卖家下架商品。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        sellerGoodsService.offShelf(id);
        return Result.success();
    }

    /**
     * 卖家标记商品已售出。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/sold")
    public Result<Void> sold(@PathVariable Long id) {
        sellerGoodsService.sold(id);
        return Result.success();
    }
}
