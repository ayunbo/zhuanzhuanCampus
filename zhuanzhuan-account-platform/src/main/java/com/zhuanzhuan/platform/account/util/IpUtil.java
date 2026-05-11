package com.zhuanzhuan.platform.account.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpUtil {

    private static final String UNKNOWN = "unknown";

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String ip = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("X-Real-IP"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("Proxy-Client-IP"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("WL-Proxy-Client-IP"));
        if (StringUtils.hasText(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static String firstValidIp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (String part : value.split(",")) {
            String ip = part.trim();
            if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }
}
