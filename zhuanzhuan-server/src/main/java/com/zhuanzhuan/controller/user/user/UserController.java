package com.zhuanzhuan.controller.user.user;

import com.zhuanzhuan.dto.UserProfileUpdateDTO;
import com.zhuanzhuan.dto.UserRegisterDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.user.user.UserService;
import com.zhuanzhuan.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器。
 */
@Tag(name = "用户资料接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success();
    }

    /**
     * 查询当前用户资料。
     */
    @Operation(summary = "查询当前用户资料")
    @GetMapping("/profile")
    public Result<UserProfileVO> getCurrentUserProfile() {
        return Result.success(userService.getCurrentUserProfile());
    }

    /**
     * 修改当前用户资料。
     */
    @Operation(summary = "修改当前用户资料")
    @PutMapping("/profile")
    public Result<Void> updateCurrentUserProfile(@RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        userService.updateCurrentUserProfile(userProfileUpdateDTO);
        return Result.success();
    }

    /**
     * 注销当前用户。
     */
    @Operation(summary = "注销当前用户")
    @DeleteMapping("/profile")
    public Result<Void> deleteCurrentUser() {
        userService.deleteCurrentUser();
        return Result.success();
    }
}