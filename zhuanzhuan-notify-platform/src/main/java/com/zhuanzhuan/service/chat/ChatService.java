package com.zhuanzhuan.service.chat;

import com.zhuanzhuan.dto.chat.ChatMessageQueryDTO;
import com.zhuanzhuan.dto.chat.ChatMessageSendDTO;
import com.zhuanzhuan.dto.chat.ChatSessionInitDTO;
import com.zhuanzhuan.dto.chat.ChatSessionReadDTO;
import com.zhuanzhuan.vo.chat.ChatMessageVO;
import com.zhuanzhuan.vo.chat.ChatSessionVO;
import com.zhuanzhuan.vo.chat.ChatUnreadCountVO;

import java.util.List;

public interface ChatService {

    /**
     * 初始化聊天会话，不存在则创建。
     */
    ChatSessionVO initSession(ChatSessionInitDTO chatSessionInitDTO);

    /**
     * 查询当前登录用户会话列表。
     */
    List<ChatSessionVO> listSessions();

    /**
     * 查询某个会话的历史消息。
     */
    List<ChatMessageVO> listMessages(ChatMessageQueryDTO chatMessageQueryDTO);

    /**
     * 发送文本消息。
     */
    ChatMessageVO sendMessage(ChatMessageSendDTO chatMessageSendDTO);

    /**
     * 将当前会话标记为已读。
     */
    void readSession(ChatSessionReadDTO chatSessionReadDTO);

    /**
     * 获取聊天未读总数。
     */
    ChatUnreadCountVO getUnreadCount();
}
