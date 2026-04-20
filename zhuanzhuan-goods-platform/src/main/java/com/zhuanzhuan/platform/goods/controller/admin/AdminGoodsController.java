package com.zhuanzhuan.platform.goods.controller.admin;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.platform.goods.service.AdminGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端商品控制器。
 */
@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController {

    @Autowired
    private AdminGoodsService adminGoodsService;

    /**
     * 管理端分页查询商品列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping({"", "/page"})
    public Result<PageResult> page(AdminGoodsPageQueryDTO dto) {
        return Result.success(adminGoodsService.page(dto));
    }

    /**
     * 管理端查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<AdminGoodsDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminGoodsService.getDetail(id));
    }

    /**
     * 管理端审核商品。
     *
     * @param id 商品 ID
     * @param dto 审核参数
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody AdminGoodsAuditDTO dto) {
        adminGoodsService.audit(id, dto);
        return Result.success();
    }

    /**
     * 管理端下架商品。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PutMapping("/{id:\\d+}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        adminGoodsService.offShelf(id);
        return Result.success();
    }

    /**
     * 管理端删除商品。
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        adminGoodsService.delete(id);
        return Result.success();
    }
}
