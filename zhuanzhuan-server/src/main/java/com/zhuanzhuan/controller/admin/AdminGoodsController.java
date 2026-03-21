package com.zhuanzhuan.controller.admin;

import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/page")
    public Result<PageResult> page(AdminGoodsPageQueryDTO dto) {
        return Result.success(goodsService.adminPage(dto));
    }

    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody AdminGoodsAuditDTO dto) {
        goodsService.adminAudit(dto);
        return Result.success();
    }
}
