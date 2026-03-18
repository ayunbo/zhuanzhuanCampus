package com.zhuanzhuan.mapper.chat;

import com.zhuanzhuan.entity.chat.ChatMessage;
import com.zhuanzhuan.vo.chat.ChatMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    /**
     * 新增一条聊天消息。
     */
    int insert(ChatMessage chatMessage);

    /**
     * 分页查询会话消息。
     */
    List<ChatMessageVO> listBySessionId(@Param("sessionId") Long sessionId,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize,
                                        @Param("currentUserId") Long currentUserId);

    /**
     * 批量将“发给当前用户且未读”的消息标记为已读。
     */
    int markReadBySession(@Param("sessionId") Long sessionId,
                          @Param("receiverId") Long receiverId,
                          @Param("readTime") LocalDateTime readTime);
}
