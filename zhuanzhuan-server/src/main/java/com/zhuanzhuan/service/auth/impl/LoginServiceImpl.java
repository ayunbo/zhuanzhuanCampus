package com.zhuanzhuan.service.auth.impl;

import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.dto.AdminLoginDTO;
import com.zhuanzhuan.dto.UserLoginDTO;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.AccountLockedException;
import com.zhuanzhuan.exception.AccountNotFoundException;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.PasswordErrorException;
import com.zhuanzhuan.mapper.administrator.AdminMapper;
import com.zhuanzhuan.mapper.user.UserMapper;
import com.zhuanzhuan.properties.JwtProperties;
import com.zhuanzhuan.service.auth.LoginService;
import com.zhuanzhuan.utils.JwtUtil;
import com.zhuanzhuan.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录业务实现
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        //1、校验登录入参是否完整
        if (adminLoginDTO == null
                || !StringUtils.hasText(adminLoginDTO.getUsername())
                || !StringUtils.hasText(adminLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        //2、提取并规范化账号密码
        String username = adminLoginDTO.getUsername().trim();
        String password = adminLoginDTO.getPassword().trim();

        //3、按账号查询管理员
        Admin admin = adminMapper.getByUsername(username);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ADMIN_ACCOUNT_NOT_FOUND);
        }

        //4、校验密码和账号状态
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (!Admin.STATUS_NORMAL.equals(admin.getStatus())) {
            throw new AccountLockedException(MessageConstant.ADMIN_ACCOUNT_DISABLED);
        }

        //5、生成管理员 JWT 并返回登录结果
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.NAME, admin.getName());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        return LoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .role(0)
                .token(token)
                .build();
    }

    @Override
    public LoginVO userLogin(UserLoginDTO userLoginDTO) {
        //1、校验登录入参对象
        if (userLoginDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、提取并规范化账号密码
        String account = trimToNull(userLoginDTO.getAccount());
        String password = trimToNull(userLoginDTO.getPassword());
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        //3、查询用户账号
        User user = userMapper.getByStudentNoOrPhone(account);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //4、校验密码和用户状态
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (!User.STATUS_NORMAL.equals(user.getStatus())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //5、生成用户 JWT 并返回登录结果
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getStudentNo());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        if (StringUtils.hasText(user.getPhone())) {
            claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        }

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return LoginVO.builder()
                .id(user.getId())
                .studentNo(user.getStudentNo())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
