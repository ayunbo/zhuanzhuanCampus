package com.zhuanzhuan.platform.account.controller.admin;

import com.zhuanzhuan.dto.AdminPageQueryDTO;
import com.zhuanzhuan.dto.AdminSaveDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.account.service.AdminService;
import com.zhuanzhuan.vo.AdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员管理接口")
@RestController
@RequestMapping("/admin/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Operation(summary = "新增管理员")
    @PostMapping
    public Result<Void> create(@RequestBody AdminSaveDTO adminSaveDTO) {
        adminService.createAdmin(adminSaveDTO);
        return Result.success();
    }

    @Operation(summary = "分页查询管理员")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        return Result.success(adminService.pageQueryAdmin(adminPageQueryDTO));
    }

    @Operation(summary = "根据ID查询管理员")
    @GetMapping("/{id}")
    public Result<AdminVO> getById(@PathVariable Long id) {
        return Result.success(adminService.getAdminById(id));
    }

    @Operation(summary = "修改管理员")
    @PutMapping
    public Result<Void> update(@RequestBody AdminSaveDTO adminSaveDTO) {
        adminService.updateAdmin(adminSaveDTO);
        return Result.success();
    }

    @Operation(summary = "删除管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Result.success();
    }
}
