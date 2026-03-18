package com.zhuanzhuan.websocket.chat;

import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.properties.JwtProperties;
import com.zhuanzhuan.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@ServerEndpoint("/ws/chat")
public class UserChatWebSocketEndpoint {

    /**
     * 当前这条 WebSocket 连接绑定的用户 id。
     */
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // 连接建立时先从 token 中解析用户身份，非法连接直接关闭。
        Long userId = resolveUserId(session);
        if (userId == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "token invalid"));
            return;
        }

        this.currentUserId = userId;
        SpringContextHolder.getBean(ChatWebSocketSessionManager.class).addSession(userId, session);
        log.info("chat websocket connected, userId={}, sessionId={}", userId, session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        if (currentUserId != null) {
            SpringContextHolder.getBean(ChatWebSocketSessionManager.class).removeSession(currentUserId, session);
            log.info("chat websocket closed, userId={}, sessionId={}", currentUserId, session.getId());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("chat websocket error, sessionId={}, message={}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 一期消息发送仍走 HTTP 接口，WebSocket 当前只负责服务端主动推送。
        log.debug("ignore client websocket message, sessionId={}, payload={}", session.getId(), message);
    }

    private Long resolveUserId(Session session) {
        Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        if (parameterMap == null) {
            return null;
        }

        List<String> tokenValues = parameterMap.get("token");
        if (tokenValues == null || tokenValues.isEmpty()) {
            return null;
        }

        String token = tokenValues.get(0);
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            // 复用项目已有 JWT 解析逻辑，得到当前连接所属用户。
            JwtProperties jwtProperties = SpringContextHolder.getBean(JwtProperties.class);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            return Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
