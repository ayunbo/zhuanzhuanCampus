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
     *
     * @param adminLoginDTO 登录信息（用户名、密码）
     * @return 登录结果（含 JWT 令牌）
     */
    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        // 1、校验用户名和密码不能为空
        if (!StringUtils.hasText(adminLoginDTO.getUsername())
                || !StringUtils.hasText(adminLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        // 2、按用户名查询管理员，去掉首尾空格兼容输入习惯
        Admin admin = adminMapper.getByUsername(adminLoginDTO.getUsername().trim());
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ADMIN_ACCOUNT_NOT_FOUND);
        }

        // 3、校验密码：入参 MD5 后与数据库存储值比对
        if (!DigestUtils.md5DigestAsHex(adminLoginDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 4、校验账号状态，被禁用的管理员不允许登录后台
        if (!AdminStatusConstant.NORMAL.equals(admin.getStatus())) {
            throw new AccountLockedException(MessageConstant.ADMIN_ACCOUNT_DISABLED);
        }

        // 5、生成 JWT 令牌，携带管理员 ID、用户名和姓名
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.NAME, admin.getName());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        // 6、封装登录结果返回
        return LoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .role(RoleConstant.ADMIN)
                .token(token)
                .build();
    }

    /**
     * 用户登录（支持学号和手机号两种登录方式）。
     *
     * @param userLoginDTO 登录信息（账号、密码）
     * @return 登录结果（含 JWT 令牌）
     */
    @Override
    public LoginVO userLogin(UserLoginDTO userLoginDTO) {
        // 1、整理账号并校验必填项，去除首尾空格后统一判断是否为空
        String account = StringUtils.hasText(userLoginDTO.getAccount())
                ? userLoginDTO.getAccount().trim() : null;
        if (!StringUtils.hasText(account) || !StringUtils.hasText(userLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.LOGIN_PARAM_EMPTY);
        }

        // 2、按学号或手机号查询账号，普通用户和卖家共用此登录入口
        User user = userMapper.getByStudentNoOrPhone(account);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3、校验密码：入参 MD5 后与数据库存储值比对
        if (!DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 4、校验账号状态，被封禁账号不允许登录前台
        if (!UserStatusConstant.NORMAL.equals(user.getStatus())) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 5、生成 JWT 令牌，携带用户 ID、学号、姓名，手机号有值时一并写入
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getStudentNo());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        if (StringUtils.hasText(user.getPhone())) {
            claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        }
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 6、封装登录结果返回
        return LoginVO.builder()
                .id(user.getId())
                .studentNo(user.getStudentNo())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
