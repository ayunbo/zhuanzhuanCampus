package com.zhuanzhuan.utils;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 业务输入校验工具
 */
public final class ValidationRuleUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");
    private static final Pattern ADMIN_USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{3,31}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[^\\s]{6,20}$");
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z0-9_\\-\\s]{1,20}$");
    private static final Pattern REAL_NAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z·\\-\\s]{2,20}$");
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://.+");
    private static final Set<String> IMAGE_SUFFIX_SET = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private ValidationRuleUtil() {
    }

    public static boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidStudentNo(String studentNo) {
        return StringUtils.hasText(studentNo) && STUDENT_NO_PATTERN.matcher(studentNo).matches();
    }

    public static boolean isValidAdminUsername(String username) {
        return StringUtils.hasText(username) && ADMIN_USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidDisplayName(String name) {
        return StringUtils.hasText(name) && DISPLAY_NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidRealName(String name) {
        return StringUtils.hasText(name) && REAL_NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidHttpUrl(String url) {
        return StringUtils.hasText(url) && HTTP_URL_PATTERN.matcher(url.trim()).matches();
    }

    public static boolean isAllowedImageFilename(String filename) {
        String extension = extractExtension(filename);
        return StringUtils.hasText(extension) && IMAGE_SUFFIX_SET.contains(extension);
    }

    public static boolean isAllowedImageUrl(String url) {
        if (!isValidHttpUrl(url)) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            return isAllowedImageFilename(uri.getPath());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String extractExtension(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        int dotIndex = value.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == value.length() - 1) {
            return "";
        }
        return value.substring(dotIndex + 1).trim().toLowerCase(Locale.ROOT);
    }
}
