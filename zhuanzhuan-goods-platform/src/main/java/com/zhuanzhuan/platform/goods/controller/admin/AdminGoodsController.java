package com.zhuanzhuan.platform.goods.controller.admin;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.platform.goods.service.AdminGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * <p>
 * 负责商品发布审核相关的后台接口，包括商品查询、详情查看、审核通过/驳回、
 * 违规商品下架以及删除。下架和删除属于风控处置动作，业务成功后会写入审核流水，
 * 用于满足审核过程可追溯的要求。
 */
@Tag(name = "管理端商品审核与风控接口")
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
    @Operation(summary = "分页查询商品发布信息", description = "支持按状态、分类、卖家、关键字和价格区间筛选，供管理员查看待审和已处理商品。")
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
    @Operation(summary = "查看商品发布详情", description = "返回商品基础信息、卖家信息、审核信息和图片列表，用于管理员审核前核对商品内容。")
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
    @Operation(summary = "审核商品发布信息", description = "仅允许处理待审核商品；审核通过后商品进入在售状态，驳回时必须填写驳回原因，并记录审核流水。")
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
    @Operation(summary = "下架违规商品", description = "仅允许下架在售商品；下架成功后自动记录审核流水，并保留商品处理前快照用于追溯。")
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
    @Operation(summary = "删除违规商品", description = "不允许删除锁定或已售出商品；删除成功后自动记录审核流水，并在删除前保存商品快照到日志详情。")
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        adminGoodsService.delete(id);
        return Result.success();
    }
}
