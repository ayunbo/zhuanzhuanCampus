package com.zhuanzhuan.platform.account.controller.admin;

import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.dto.SellerAuthAuditDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.account.service.SellerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员卖家审核接口")
@RestController
@RequestMapping("/admin/seller-auth")
public class AdminSellerAuthController {

    @Autowired
    private SellerAuthService sellerAuthService;

    @Operation(summary = "分页查询卖家申请")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(AdminSellerAuthPageQueryDTO pageQueryDTO) {
        return Result.success(sellerAuthService.pageQuerySellerAuth(pageQueryDTO));
    }

    @Operation(summary = "审核卖家申请（通过/驳回）")
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody SellerAuthAuditDTO sellerAuthAuditDTO) {
        sellerAuthService.auditSellerAuth(sellerAuthAuditDTO);
        return Result.success();
    }
}
