package com.zhuanzhuan.mapper.chat;

import com.zhuanzhuan.entity.chat.ChatSession;
import com.zhuanzhuan.vo.chat.ChatSessionCreateSupportVO;
import com.zhuanzhuan.vo.chat.ChatSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatSessionMapper {

    /**
     * 根据会话 id 查询会话。
     */
    ChatSession getById(Long id);

    /**
     * 根据 商品 + 卖家 + 买家 查询唯一会话。
     * 这是聊天模块最核心的唯一约束。
     */
    ChatSession getByUnique(@Param("goodsId") Long goodsId, @Param("sellerId") Long sellerId, @Param("buyerId") Long buyerId);

    /**
     * 新增聊天会话。
     */
    int insert(ChatSession chatSession);

    /**
     * 发送消息后更新会话摘要、最后时间，并给对方未读数 +1。
     */
    int updateAfterSend(@Param("sessionId") Long sessionId,
                        @Param("lastMsg") String lastMsg,
                        @Param("lastTime") LocalDateTime lastTime,
                        @Param("senderId") Long senderId,
                        @Param("sellerId") Long sellerId,
                        @Param("buyerId") Long buyerId,
                        @Param("updateUser") Long updateUser);

    /**
     * 当前用户查看会话后，清空自己这一侧的会话未读数。
     */
    int clearUnreadForUser(@Param("sessionId") Long sessionId,
                           @Param("currentUserId") Long currentUserId,
                           @Param("updateUser") Long updateUser);

    /**
     * 查询当前用户参与的所有会话。
     */
    List<ChatSessionVO> listByUserId(Long userId);

    /**
     * 汇总当前用户在所有会话中的未读数。
     */
    Integer sumUnreadByUserId(Long userId);

    /**
     * 创建会话时，从 goods 表查询商品快照和卖家信息。
     */
    ChatSessionCreateSupportVO getCreateSupportByGoodsId(Long goodsId);
}
