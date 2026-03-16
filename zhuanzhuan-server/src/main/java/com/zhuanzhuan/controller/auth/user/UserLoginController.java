package com.zhuanzhuan.controller.auth.user;

import com.zhuanzhuan.dto.UserLoginDTO;
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
 * 用户登录控制器
 */
@Tag(name = "用户登录接口")
@RestController
@RequestMapping("/user/login")
public class UserLoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 用户统一登录（普通用户、卖家共用）
     */
    @Operation(summary = "用户统一登录（学号或手机号）")
    @PostMapping
    public Result<LoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(loginService.userLogin(userLoginDTO));
    }

    /**
     * 兼容旧版买家登录地址，内部仍走统一登录逻辑。
     */
    @Operation(summary = "兼容旧版买家登录地址")
    @PostMapping("/buyer")
    public Result<LoginVO> buyerLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(loginService.userLogin(userLoginDTO));
    }

    /**
     * 兼容旧版卖家登录地址，内部仍走统一登录逻辑。
     */
    @Operation(summary = "兼容旧版卖家登录地址")
    @PostMapping("/seller")
    public Result<LoginVO> sellerLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(loginService.userLogin(userLoginDTO));
    }
}
