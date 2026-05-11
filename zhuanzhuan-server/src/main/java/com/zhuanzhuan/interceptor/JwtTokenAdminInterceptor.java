package com.zhuanzhuan.interceptor;

import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.constant.AdminStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.platform.account.mapper.AdminMapper;
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
 * 管理员 JWT 校验拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        BaseContext.removeCurrentId();

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            Integer status = riskControlService.getAuthStatus("admin", adminId);
            if (status == null) {
                Admin admin = adminMapper.getById(adminId);
                status = admin == null ? AdminStatusConstant.DISABLED : admin.getStatus();
                riskControlService.cacheAuthStatus("admin", adminId, status);
            }
            if (!AdminStatusConstant.NORMAL.equals(status)) {
                response.setStatus(401);
                return false;
            }
            BaseContext.setCurrentId(adminId);
            log.debug("管理员 JWT 解析成功, adminId={}", adminId);
            return true;
        } catch (Exception ex) {
            log.warn("管理员 JWT 校验失败, uri={}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
