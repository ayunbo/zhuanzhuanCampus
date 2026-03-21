package com.zhuanzhuan.platform.account.controller.admin;

import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.dto.AdminUserSaveDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.account.service.AdminUserService;
import com.zhuanzhuan.vo.UserProfileVO;
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

@Tag(name = "管理员用户管理接口")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> create(@RequestBody AdminUserSaveDTO saveDTO) {
        adminUserService.createUser(saveDTO);
        return Result.success();
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult> page(AdminUserPageQueryDTO pageQueryDTO) {
        return Result.success(adminUserService.pageQueryUser(pageQueryDTO));
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserProfileVO> getById(@PathVariable Long id) {
        return Result.success(adminUserService.getUserById(id));
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody AdminUserSaveDTO saveDTO) {
        adminUserService.updateUser(saveDTO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success();
    }
}
