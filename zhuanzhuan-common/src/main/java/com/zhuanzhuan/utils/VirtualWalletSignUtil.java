package com.zhuanzhuan.utils;

import com.zhuanzhuan.dto.VirtualWalletCallbackDTO;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VirtualWalletSignUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private VirtualWalletSignUtil() {
    }

    public static String buildPayload(VirtualWalletCallbackDTO dto) {
        return safe(dto.getRequestNo()) + "|" +
                dto.getOrderId() + "|" +
                normalizeAmount(dto.getAmount()) + "|" +
                dto.getPayStatus() + "|" +
                safe(dto.getTransactionNo()) + "|" +
                safe(dto.getWalletUserId()) + "|" +
                safe(dto.getMessage()) + "|" +
                dto.getTimestamp();
    }

    public static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign virtual wallet payload", e);
        }
    }

    public static boolean verify(VirtualWalletCallbackDTO dto, String secret) {
        if (!StringUtils.hasText(dto.getSign())) {
            return false;
        }
        String expected = sign(buildPayload(dto), secret);
        return expected.equals(dto.getSign());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String normalizeAmount(BigDecimal amount) {
        return amount == null ? "" : amount.stripTrailingZeros().toPlainString();
    }
}
