package com.zhuanzhuan.platform.account.service.auth.impl;

import com.zhuanzhuan.constant.AdminStatusConstant;
import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.dto.AdminLoginDTO;
import com.zhuanzhuan.dto.UserLoginDTO;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.AccountLockedException;
import com.zhuanzhuan.exception.AccountNotFoundException;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.PasswordErrorException;
import com.zhuanzhuan.platform.account.mapper.AdminMapper;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.account.service.auth.LoginService;
import com.zhuanzhuan.properties.JwtProperties;
import com.zhuanzhuan.utils.JwtUtil;
import com.zhuanzhuan.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录业务实现。
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录。
     */
    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        // 1、校验登录参数
        if (adminLoginDTO == null
                || !StringUtils.hasText(adminLoginDTO.getUsername())
                || !StringUtils.hasText(adminLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        // 2、整理账号，避免因为首尾空格导致后台账号查不到
        String username = adminLoginDTO.getUsername().trim();
        String password = adminLoginDTO.getPassword();

        // 3、按用户名查询管理员账号，后台登录只允许管理员账号进入
        Admin admin = adminMapper.getByUsername(username);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ADMIN_ACCOUNT_NOT_FOUND);
        }

        // 4、先校验密码，再校验账号状态，禁用管理员不能继续进入后台
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (!AdminStatusConstant.NORMAL.equals(admin.getStatus())) {
            throw new AccountLockedException(MessageConstant.ADMIN_ACCOUNT_DISABLED);
        }

        // 5、生成令牌并返回登录结果
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.NAME, admin.getName());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );

        return LoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .role(RoleConstant.ADMIN)
                .token(token)
                .build();
    }

    /**
     * 用户登录。
     */
    @Override
    public LoginVO userLogin(UserLoginDTO userLoginDTO) {
        // 1、校验登录参数
        if (userLoginDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理登录账号，统一去掉首尾空格，兼容学号和手机号两种登录方式
        String account = userLoginDTO.getAccount();
        if (StringUtils.hasText(account)) {
            account = account.trim();
        } else {
            account = null;
        }

        String password = userLoginDTO.getPassword();

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        // 3、按学号或手机号查询账号，普通用户和卖家共用这一套登录入口
        User user = userMapper.getByStudentNoOrPhone(account);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 4、校验密码和账号状态，被封禁账号不能继续登录前台
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (!UserStatusConstant.NORMAL.equals(user.getStatus())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 5、生成令牌并返回登录结果
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getStudentNo());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        if (StringUtils.hasText(user.getPhone())) {
            claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        }

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        return LoginVO.builder()
                .id(user.getId())
                .studentNo(user.getStudentNo())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
