package com.zhuanzhuan.controller.user.chat;

import com.zhuanzhuan.dto.chat.ChatMessageQueryDTO;
import com.zhuanzhuan.dto.chat.ChatMessageSendDTO;
import com.zhuanzhuan.dto.chat.ChatSessionInitDTO;
import com.zhuanzhuan.dto.chat.ChatSessionReadDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.chat.ChatService;
import com.zhuanzhuan.vo.chat.ChatMessageVO;
import com.zhuanzhuan.vo.chat.ChatSessionVO;
import com.zhuanzhuan.vo.chat.ChatUnreadCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户聊天接口")
@RestController
@RequestMapping("/user/chat")
public class UserChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 前端进入聊天页时先调这个接口。
     * 后端会按唯一规则查会话，不存在时才创建。
     */
    @Operation(summary = "初始化聊天会话，不存在则创建")
    @PostMapping("/session/init")
    public Result<ChatSessionVO> initSession(@RequestBody ChatSessionInitDTO chatSessionInitDTO) {
        return Result.success(chatService.initSession(chatSessionInitDTO));
    }

    /**
     * 聊天首页会话列表接口。
     */
    @Operation(summary = "查询当前用户会话列表")
    @GetMapping("/session/list")
    public Result<List<ChatSessionVO>> listSessions() {
        return Result.success(chatService.listSessions());
    }

    /**
     * 历史消息分页接口。
     */
    @Operation(summary = "查询会话消息列表")
    @GetMapping("/message/list")
    public Result<List<ChatMessageVO>> listMessages(ChatMessageQueryDTO chatMessageQueryDTO) {
        return Result.success(chatService.listMessages(chatMessageQueryDTO));
    }

    /**
     * 文本消息发送接口。
     */
    @Operation(summary = "发送文本消息")
    @PostMapping("/message/send")
    public Result<ChatMessageVO> sendMessage(@RequestBody ChatMessageSendDTO chatMessageSendDTO) {
        return Result.success(chatService.sendMessage(chatMessageSendDTO));
    }

    /**
     * 进入会话后调用，用于清空该会话未读。
     */
    @Operation(summary = "将当前会话标记为已读")
    @PatchMapping("/session/read")
    public Result<Void> readSession(@RequestBody ChatSessionReadDTO chatSessionReadDTO) {
        chatService.readSession(chatSessionReadDTO);
        return Result.success();
    }

    /**
     * 聊天全局未读角标接口。
     */
    @Operation(summary = "查询当前用户聊天未读总数")
    @GetMapping("/unread/count")
    public Result<ChatUnreadCountVO> getUnreadCount() {
        return Result.success(chatService.getUnreadCount());
    }
}
