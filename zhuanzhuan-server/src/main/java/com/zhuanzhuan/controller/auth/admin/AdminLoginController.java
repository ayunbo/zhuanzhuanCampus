package com.zhuanzhuan.controller.auth.admin;

import com.zhuanzhuan.dto.AdminLoginDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.auth.LoginService;
import com.zhuanzhuan.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录控制器。
 */
@Tag(name = "管理员登录接口")
@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 管理员登录。
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        return Result.success(loginService.adminLogin(adminLoginDTO));
    }
}