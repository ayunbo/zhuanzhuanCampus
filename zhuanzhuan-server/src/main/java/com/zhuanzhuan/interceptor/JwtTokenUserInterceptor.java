package com.zhuanzhuan.interceptor;

import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.properties.JwtProperties;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户 JWT 校验拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        BaseContext.removeCurrentId();

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            Integer status = riskControlService.getAuthStatus("user", userId);
            if (status == null) {
                User user = userMapper.getById(userId);
                status = user == null ? UserStatusConstant.BANNED : user.getStatus();
                riskControlService.cacheAuthStatus("user", userId, status);
            }
            if (!UserStatusConstant.NORMAL.equals(status)) {
                response.setStatus(401);
                return false;
            }
            BaseContext.setCurrentId(userId);
            log.debug("用户 JWT 解析成功, userId={}", userId);
            return true;
        } catch (Exception ex) {
            log.warn("用户 JWT 校验失败, uri={}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
