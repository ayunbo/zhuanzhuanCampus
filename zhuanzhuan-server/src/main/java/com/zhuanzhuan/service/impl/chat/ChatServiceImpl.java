package com.zhuanzhuan.service.impl.chat;

import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.chat.ChatMessageQueryDTO;
import com.zhuanzhuan.dto.chat.ChatMessageSendDTO;
import com.zhuanzhuan.dto.chat.ChatSessionInitDTO;
import com.zhuanzhuan.dto.chat.ChatSessionReadDTO;
import com.zhuanzhuan.entity.chat.ChatMessage;
import com.zhuanzhuan.entity.chat.ChatSession;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.mapper.chat.ChatMessageMapper;
import com.zhuanzhuan.mapper.chat.ChatSessionMapper;
import com.zhuanzhuan.service.chat.ChatService;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.chat.ChatMessageVO;
import com.zhuanzhuan.vo.chat.ChatPushMessageVO;
import com.zhuanzhuan.vo.chat.ChatReadReceiptVO;
import com.zhuanzhuan.vo.chat.ChatSessionCreateSupportVO;
import com.zhuanzhuan.vo.chat.ChatSessionVO;
import com.zhuanzhuan.vo.chat.ChatUnreadCountVO;
import com.zhuanzhuan.websocket.chat.ChatWebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    /**
     * 一期分页和文本长度的基础限制。
     */
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_CONTENT_LENGTH = 1000;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatWebSocketPublisher chatWebSocketPublisher;

    @Override
    @Transactional
    public ChatSessionVO initSession(ChatSessionInitDTO chatSessionInitDTO) {
        if (chatSessionInitDTO == null) {
            throw new BaseException("请求参数不能为空");
        }

        Long currentUserId = getCurrentUserId();
        Long goodsId = chatSessionInitDTO.getGoodsId();
        Long buyerId = chatSessionInitDTO.getBuyerId();
        Long sellerId = chatSessionInitDTO.getSellerId();

        if (goodsId == null || buyerId == null || sellerId == null) {
            throw new BaseException("goodsId、buyerId、sellerId 不能为空");
        }
        if (buyerId.equals(sellerId)) {
            throw new BaseException("买家和卖家不能是同一用户");
        }
        if (!currentUserId.equals(buyerId) && !currentUserId.equals(sellerId)) {
            throw new BaseException("当前用户无权创建该会话");
        }
        if (userMapper.getById(buyerId) == null || userMapper.getById(sellerId) == null) {
            throw new BaseException("聊天用户不存在");
        }

        // 先按“商品 + 买家 + 卖家”的唯一规则查会话，防止重复创建。
        ChatSession session = chatSessionMapper.getByUnique(goodsId, sellerId, buyerId);
        if (session == null) {
            ChatSessionCreateSupportVO supportVO = chatSessionMapper.getCreateSupportByGoodsId(goodsId);
            if (supportVO == null) {
                throw new BaseException("商品不存在");
            }
            if (!sellerId.equals(supportVO.getSellerId())) {
                throw new BaseException("卖家与商品信息不匹配");
            }

            LocalDateTime now = LocalDateTime.now();
            session = ChatSession.builder()
                    .id(IdGenerator.nextId())
                    .goodsId(goodsId)
                    .sellerId(sellerId)
                    .buyerId(buyerId)
                    .goodsTitle(supportVO.getGoodsTitle())
                    .goodsCover(supportVO.getGoodsCover())
                    .sellerUnread(0)
                    .buyerUnread(0)
                    .createTime(now)
                    .updateTime(now)
                    .createUser(currentUserId)
                    .updateUser(currentUserId)
                    .build();
            int rows = chatSessionMapper.insert(session);
            if (rows <= 0) {
                throw new BaseException("聊天会话创建失败");
            }
        }

        // 就算会话存在，也要再次确认当前用户确实属于该会话。
        ensureSessionMember(session, currentUserId);
        return buildSessionDetail(session, currentUserId);
    }

    @Override
    public List<ChatSessionVO> listSessions() {
        Long currentUserId = getCurrentUserId();
        List<ChatSessionVO> sessionList = chatSessionMapper.listByUserId(currentUserId);
        return sessionList == null ? Collections.emptyList() : sessionList;
    }

    @Override
    public List<ChatMessageVO> listMessages(ChatMessageQueryDTO chatMessageQueryDTO) {
        if (chatMessageQueryDTO == null || chatMessageQueryDTO.getSessionId() == null) {
            throw new BaseException("sessionId 不能为空");
        }

        Long currentUserId = getCurrentUserId();
        ChatSession session = getAuthorizedSession(chatMessageQueryDTO.getSessionId(), currentUserId);
        int pageNo = normalizePageNo(chatMessageQueryDTO.getPageNo());
        int pageSize = normalizePageSize(chatMessageQueryDTO.getPageSize());
        // MySQL 分页使用 offset + pageSize。
        int offset = (pageNo - 1) * pageSize;

        List<ChatMessageVO> messageList = chatMessageMapper.listBySessionId(session.getId(), offset, pageSize, currentUserId);
        return messageList == null ? Collections.emptyList() : messageList;
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageSendDTO chatMessageSendDTO) {
        if (chatMessageSendDTO == null || chatMessageSendDTO.getSessionId() == null) {
            throw new BaseException("sessionId 不能为空");
        }

        Long currentUserId = getCurrentUserId();
        ChatSession session = getAuthorizedSession(chatMessageSendDTO.getSessionId(), currentUserId);
        String content = trimToNull(chatMessageSendDTO.getContent());
        if (!StringUtils.hasText(content)) {
            throw new BaseException("消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BaseException("消息内容长度不能超过 1000");
        }

        // 根据当前用户在会话中的身份推导“对方”是谁。
        Long receiverId = session.getSellerId().equals(currentUserId) ? session.getBuyerId() : session.getSellerId();
        LocalDateTime now = LocalDateTime.now();
        ChatMessage chatMessage = ChatMessage.builder()
                .id(IdGenerator.nextId())
                .sessionId(session.getId())
                .senderId(currentUserId)
                .receiverId(receiverId)
                .type(ChatMessage.TYPE_TEXT)
                .content(content)
                .readStatus(ChatMessage.READ_UNREAD)
                .createTime(now)
                .updateTime(now)
                .createUser(currentUserId)
                .updateUser(currentUserId)
                .build();

        int insertRows = chatMessageMapper.insert(chatMessage);
        if (insertRows <= 0) {
            throw new BaseException("消息发送失败");
        }

        // 一条消息发出后，会话表也要同步更新摘要、时间和未读数。
        int updateRows = chatSessionMapper.updateAfterSend(
                session.getId(),
                buildLastMsg(content),
                now,
                currentUserId,
                session.getSellerId(),
                session.getBuyerId(),
                currentUserId
        );
        if (updateRows <= 0) {
            throw new BaseException("会话状态更新失败");
        }

        // HTTP 响应返回给发送方一份“mine=true”的消息对象。
        ChatMessageVO senderMessageVO = buildMessageVO(chatMessage, true);
        // WebSocket 只给接收方推新消息；未读数变更则给双方都推，方便前端角标同步。
        chatWebSocketPublisher.sendToUser(receiverId, new ChatPushMessageVO<>("chat.message", buildMessageVO(chatMessage, false)));
        chatWebSocketPublisher.sendToUser(receiverId, new ChatPushMessageVO<>("chat.unread", getUnreadCountByUserId(receiverId)));
        chatWebSocketPublisher.sendToUser(currentUserId, new ChatPushMessageVO<>("chat.unread", getUnreadCountByUserId(currentUserId)));
        return senderMessageVO;
    }

    @Override
    @Transactional
    public void readSession(ChatSessionReadDTO chatSessionReadDTO) {
        if (chatSessionReadDTO == null || chatSessionReadDTO.getSessionId() == null) {
            throw new BaseException("sessionId 不能为空");
        }

        Long currentUserId = getCurrentUserId();
        ChatSession session = getAuthorizedSession(chatSessionReadDTO.getSessionId(), currentUserId);
        LocalDateTime now = LocalDateTime.now();
        List<Long> unreadMessageIds = chatMessageMapper.listUnreadMessageIdsBySession(session.getId(), currentUserId);
        // 这里实现的是“进入会话后批量已读”，不是逐条点击已读。
        chatMessageMapper.markReadBySession(session.getId(), currentUserId, now);
        chatSessionMapper.clearUnreadForUser(session.getId(), currentUserId, currentUserId);
        chatWebSocketPublisher.sendToUser(currentUserId, new ChatPushMessageVO<>("chat.unread", getUnreadCountByUserId(currentUserId)));
        if (unreadMessageIds != null && !unreadMessageIds.isEmpty()) {
            Long otherUserId = session.getSellerId().equals(currentUserId) ? session.getBuyerId() : session.getSellerId();
            ChatReadReceiptVO receiptVO = new ChatReadReceiptVO(session.getId(), currentUserId, now, unreadMessageIds);
            chatWebSocketPublisher.sendToUser(otherUserId, new ChatPushMessageVO<>("chat.read", receiptVO));
        }
    }

    @Override
    public ChatUnreadCountVO getUnreadCount() {
        return getUnreadCountByUserId(getCurrentUserId());
    }

    private ChatUnreadCountVO getUnreadCountByUserId(Long userId) {
        Integer totalUnread = chatSessionMapper.sumUnreadByUserId(userId);
        return new ChatUnreadCountVO(totalUnread == null ? 0 : totalUnread);
    }

    private ChatSession getAuthorizedSession(Long sessionId, Long currentUserId) {
        ChatSession session = chatSessionMapper.getById(sessionId);
        if (session == null) {
            throw new BaseException("聊天会话不存在");
        }
        // 会话存在不代表有权限访问，必须是 buyer 或 seller 之一。
        ensureSessionMember(session, currentUserId);
        return session;
    }

    private void ensureSessionMember(ChatSession session, Long currentUserId) {
        if (!currentUserId.equals(session.getSellerId()) && !currentUserId.equals(session.getBuyerId())) {
            throw new BaseException("当前用户无权访问该会话");
        }
    }

    private ChatSessionVO buildSessionDetail(ChatSession session, Long currentUserId) {
        // 优先复用会话列表查询结果，确保单个会话返回结构与列表一致。
        List<ChatSessionVO> sessionList = chatSessionMapper.listByUserId(currentUserId);
        if (sessionList != null) {
            for (ChatSessionVO sessionVO : sessionList) {
                if (session.getId().equals(sessionVO.getSessionId())) {
                    return sessionVO;
                }
            }
        }

        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(session.getId());
        vo.setGoodsId(session.getGoodsId());
        vo.setSellerId(session.getSellerId());
        vo.setBuyerId(session.getBuyerId());
        vo.setGoodsTitle(session.getGoodsTitle());
        vo.setGoodsCover(session.getGoodsCover());
        vo.setLastMsg(session.getLastMsg());
        vo.setLastTime(session.getLastTime());
        if (currentUserId.equals(session.getSellerId())) {
            vo.setMyRole("seller");
            vo.setUnreadCount(session.getSellerUnread());
            vo.setTargetUserId(session.getBuyerId());
        } else {
            vo.setMyRole("buyer");
            vo.setUnreadCount(session.getBuyerUnread());
            vo.setTargetUserId(session.getSellerId());
        }
        return vo;
    }

    private ChatMessageVO buildMessageVO(ChatMessage chatMessage, boolean mine) {
        // 统一封装消息返回体，避免 HTTP 与 WebSocket 字段不一致。
        ChatMessageVO messageVO = new ChatMessageVO();
        messageVO.setId(chatMessage.getId());
        messageVO.setSessionId(chatMessage.getSessionId());
        messageVO.setSenderId(chatMessage.getSenderId());
        messageVO.setReceiverId(chatMessage.getReceiverId());
        messageVO.setType(chatMessage.getType());
        messageVO.setContent(chatMessage.getContent());
        messageVO.setReadStatus(chatMessage.getReadStatus());
        messageVO.setReadTime(chatMessage.getReadTime());
        messageVO.setCreateTime(chatMessage.getCreateTime());
        messageVO.setMine(mine);
        return messageVO;
    }

    private Long getCurrentUserId() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException("用户未登录");
        }
        return currentUserId;
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String buildLastMsg(String content) {
        // 会话摘要只保留较短文本，便于列表展示。
        return content.length() > 100 ? content.substring(0, 100) : content;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
