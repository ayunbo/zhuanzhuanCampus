# 前端对接文档：聊天模块 / 通知模块

## 1. 文档用途

这个文档给前端项目使用，目标是让前端先明确：

- 当前聊天模块已经做到什么程度
- 通知模块当前是什么状态
- 后端接口怎么调用
- WebSocket 怎么连接
- 字段含义和页面交互应该怎么理解

当前文档基于现有项目代码、数据库 `zhuanzhuan.sql`、以及《聊天与通知全栈设计与规范.md》整理。

---

## 2. 项目背景

本项目是校园二手交易系统，聊天和通知都不是普通社交场景，而是围绕商品交易流程展开。

技术背景：

- 后端：Spring Boot + Spring + MyBatis
- 数据库：MySQL
- 登录态：JWT
- 当前用户：后端通过 `BaseContext` 从 token 中取当前用户 id
- 返回格式：统一 `Result<T>`
- 前后端分离：前端是单独项目
- 实时推送：WebSocket

后端接口风格：

- 用户端统一前缀：`/user/...`
- 管理端统一前缀：`/admin/...`
- 聊天模块一期只做用户端
- 通知模块一期也按用户端设计

统一返回格式：

```json
{
  "code": 1,
  "msg": null,
  "data": {}
}
```

说明：

- `code = 1` 表示成功
- `code = 0` 表示失败
- 失败时主要看 `msg`

---

## 3. 聊天模块业务理解

## 3.1 聊天不是普通私聊

聊天是围绕“商品交易”产生的沟通能力，不是任意用户之间自由聊天。

聊天会话由这 3 个字段唯一确定：

- `goodsId`
- `buyerId`
- `sellerId`

也就是说，同一个商品、同一个买家、同一个卖家，只能有一个会话。

---

## 3.2 身份判断规则

用户在系统全局里可以既是卖家，也可以是买家。

所以前端不要根据用户表里的全局 `role` 去判断某个聊天会话里的身份，而要根据当前会话里的：

- `sellerId`
- `buyerId`

来判断。

后端在会话列表里已经直接返回了：

- `myRole`

取值：

- `seller`
- `buyer`

前端直接用这个字段即可。

---

## 3.3 当前聊天一期范围

已支持：

- 初始化会话
- 会话列表
- 历史消息列表
- 发送文本消息
- 会话已读
- 聊天未读总数
- WebSocket 在线消息推送

暂不支持：

- 图片消息
- 撤回消息
- 删除会话
- 删除消息
- 消息搜索
- 已读回执的精细同步 UI

---

## 4. 通知模块当前状态

通知模块当前还没有开始写后端接口。

目前只有统一业务约束已经明确：

- 通知本质上是一套统一的站内通知系统
- 后续会覆盖订单通知、审核通知、举报通知、系统通知
- 后续路径会使用 `/user/notice/...`
- 在线通知也会走 WebSocket

当前前端可以先做的只有：

- 预留通知中心页面结构
- 预留通知列表、未读数、已读态样式
- 预留 WebSocket 事件处理扩展位

当前不能直接联调通知接口，因为还没落库到代码里。

---

## 5. 聊天相关数据库理解

## 5.1 `chatsession`

会话表，用来支撑会话列表。

关键字段：

- `id`：会话 id
- `goods_id`
- `seller_id`
- `buyer_id`
- `goods_title`
- `goods_cover`
- `last_msg`
- `last_time`
- `seller_unread`
- `buyer_unread`

前端重点理解：

- 会话列表不是通过消息表自己聚合，而是直接读会话表
- 未读数是会话级缓存字段，不需要前端自己算

---

## 5.2 `chatmessage`

消息表，用来支撑聊天记录。

关键字段：

- `id`
- `session_id`
- `sender_id`
- `receiver_id`
- `type`
- `content`
- `read_status`
- `read_time`
- `create_time`

当前一期里：

- `type = 1` 文本
- 其他类型先不用

---

## 6. 鉴权说明

除登录、注册等公开接口外，其它用户接口都默认要带 token。

前端调用聊天接口时需要在请求头中带：

```http
token: {JWT}
```

WebSocket 连接时，当前后端实现是通过 query 参数传 token：

```text
ws://{host}/ws/chat?token={JWT}
```

---

## 7. 聊天 REST 接口

## 7.1 初始化聊天会话

用途：

- 进入聊天页前先调
- 如果会话不存在则创建
- 如果已存在则返回已有会话

接口：

```http
POST /user/chat/session/init
```

请求体：

```json
{
  "goodsId": 10001,
  "buyerId": 2,
  "sellerId": 5
}
```

字段说明：

- `goodsId`：商品 id
- `buyerId`：买家 id
- `sellerId`：卖家 id

说明：

- 当前登录用户必须是 `buyerId` 或 `sellerId` 其中之一
- 一般买家发起聊天时，前端会更常调用这个接口
- 如果前端是从商品详情进入聊天，通常你已经知道 `goodsId` 和 `sellerId`

成功返回示例：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "sessionId": 174176000001,
    "goodsId": 10001,
    "sellerId": 5,
    "buyerId": 2,
    "goodsTitle": "二手显示器",
    "goodsCover": "https://xxx/cover.png",
    "lastMsg": null,
    "lastTime": null,
    "unreadCount": 0,
    "myRole": "buyer",
    "targetUserId": 5,
    "targetUserName": "张三",
    "targetUserAvatar": "https://xxx/avatar.png"
  }
}
```

---

## 7.2 查询会话列表

接口：

```http
GET /user/chat/session/list
```

用途：

- 聊天首页会话列表
- 最近联系人列表

成功返回示例：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "sessionId": 174176000001,
      "goodsId": 10001,
      "sellerId": 5,
      "buyerId": 2,
      "goodsTitle": "二手显示器",
      "goodsCover": "https://xxx/cover.png",
      "lastMsg": "这个还在吗？",
      "lastTime": "2026-03-16T10:30:00",
      "unreadCount": 2,
      "myRole": "buyer",
      "targetUserId": 5,
      "targetUserName": "张三",
      "targetUserAvatar": "https://xxx/avatar.png"
    }
  ]
}
```

字段说明：

- `unreadCount`：当前登录用户在该会话的未读数
- `myRole`：当前用户在该会话里是 `seller` 还是 `buyer`
- `targetUser...`：聊天对方的信息

前端建议：

- 按 `lastTime` 倒序展示
- `lastTime = null` 的新会话可放后面
- `unreadCount > 0` 时显示红点或角标

---

## 7.3 查询消息列表

接口：

```http
GET /user/chat/message/list?sessionId=174176000001&pageNo=1&pageSize=20
```

参数：

- `sessionId`：必填
- `pageNo`：默认 1
- `pageSize`：默认 20，当前最大 100

成功返回示例：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "id": 174176000099,
      "sessionId": 174176000001,
      "senderId": 2,
      "receiverId": 5,
      "type": 1,
      "content": "这个还在吗？",
      "readStatus": 0,
      "readTime": null,
      "createTime": "2026-03-16T10:30:00",
      "mine": true
    }
  ]
}
```

字段说明：

- `mine = true`：这条消息是当前登录用户自己发的
- `mine = false`：是对方发来的

重要说明：

- 当前后端 SQL 返回顺序是按时间倒序
- 前端如果聊天窗口希望“旧消息在上，新消息在下”，需要自己反转一次列表再渲染

---

## 7.4 发送文本消息

接口：

```http
POST /user/chat/message/send
```

请求体：

```json
{
  "sessionId": 174176000001,
  "content": "这个还在吗？"
}
```

约束：

- `sessionId` 必填
- `content` 必填
- 当前最长 1000 字
- 当前仅支持文本

成功返回示例：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 174176000099,
    "sessionId": 174176000001,
    "senderId": 2,
    "receiverId": 5,
    "type": 1,
    "content": "这个还在吗？",
    "readStatus": 0,
    "readTime": null,
    "createTime": "2026-03-16T10:30:00",
    "mine": true
  }
}
```

发送成功后后端会自动做这些事：

- 往 `chatmessage` 插一条消息
- 更新 `chatsession.lastMsg`
- 更新 `chatsession.lastTime`
- 给对方未读数 `+1`
- 如果对方在线，走 WebSocket 推送新消息

前端建议：

- 可以先乐观插入本地消息，收到 HTTP 成功后再以服务端返回结果覆盖
- 或者直接以 HTTP 返回为准插入消息列表

---

## 7.5 会话标记已读

接口：

```http
PATCH /user/chat/session/read
```

请求体：

```json
{
  "sessionId": 174176000001
}
```

用途：

- 用户进入某个聊天窗口后调用
- 将“发给当前用户且未读”的消息标记为已读
- 同时清空当前用户在该会话中的未读数

前端建议调用时机：

- 打开聊天详情页后立即调用一次
- 或页面真正进入可见状态后调用

---

## 7.6 查询聊天未读总数

接口：

```http
GET /user/chat/unread/count
```

成功返回示例：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "totalUnreadCount": 3
  }
}
```

用途：

- 顶部聊天角标
- tabBar 红点
- 消息中心入口红点

---

## 8. WebSocket 对接

## 8.1 连接方式

连接地址：

```text
ws://{host}/ws/chat?token={JWT}
```

示例：

```text
ws://localhost:8080/ws/chat?token=eyJhbGciOiJIUzI1NiJ9...
```

说明：

- 连接时必须带 token
- token 无效时连接会被服务端关闭

---

## 8.2 当前后端会主动推送的事件

当前已经实现两类事件：

- `chat.message`
- `chat.unread`

统一结构：

```json
{
  "event": "chat.message",
  "data": {}
}
```

---

## 8.3 `chat.message`

表示收到一条新的聊天消息。

示例：

```json
{
  "event": "chat.message",
  "data": {
    "id": 174176000099,
    "sessionId": 174176000001,
    "senderId": 2,
    "receiverId": 5,
    "type": 1,
    "content": "这个还在吗？",
    "readStatus": 0,
    "readTime": null,
    "createTime": "2026-03-16T10:30:00",
    "mine": false
  }
}
```

前端处理建议：

- 如果当前正停留在对应 `sessionId` 页面，直接插入消息列表
- 如果不在当前聊天页，更新对应会话的最后一条消息和未读 UI
- 如本地没有该会话，可重新拉一次会话列表

---

## 8.4 `chat.unread`

表示聊天未读总数更新。

示例：

```json
{
  "event": "chat.unread",
  "data": {
    "totalUnreadCount": 3
  }
}
```

前端处理建议：

- 全局 store 中维护聊天未读总数
- 收到该事件后直接覆盖全局红点计数

---

## 9. 前端页面建议

## 9.1 聊天首页

可以做这些区域：

- 会话列表
- 每个会话展示商品图、商品标题、对方昵称、最后消息、最后时间、未读数

关键字段来源：

- `goodsCover`
- `goodsTitle`
- `targetUserName`
- `lastMsg`
- `lastTime`
- `unreadCount`

---

## 9.2 聊天详情页

建议页面参数至少包括：

- `sessionId`

如果从商品详情进入，也可以先带：

- `goodsId`
- `sellerId`
- `buyerId`

建议流程：

1. 先调 `/user/chat/session/init`
2. 拿到 `sessionId`
3. 再拉 `/user/chat/message/list`
4. 进入页后调用 `/user/chat/session/read`
5. 同时保持 WebSocket 连接

---

## 9.3 全局状态建议

前端建议全局维护：

- `chatUnreadTotal`
- `chatSessionList`
- `currentChatSessionId`
- `chatSocketConnected`

这样聊天页、首页角标、tabBar 红点可以共用状态。

---

## 10. 已知限制和注意事项

## 10.1 当前消息列表顺序

后端当前按倒序返回消息列表，所以前端若按正常聊天顺序展示，需要反转。

---

## 10.2 当前只做文本消息

所以输入框当前只需要支持：

- 文本输入
- 发送按钮

不需要先做：

- 图片选择
- 文件发送
- 语音

---

## 10.3 当前通知模块未落地

所以前端不要直接请求：

- `/user/notice/...`

否则会 404。

---

## 10.4 Swagger 注解中文乱码

如果你在某些查看工具里看到个别注解说明乱码，那是终端显示问题，不影响接口实际调用和字段命名。

---

## 11. 当前已落代码位置

聊天控制器：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/controller/user/chat/UserChatController.java`

聊天业务：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/service/chat/ChatService.java`
- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/service/impl/chat/ChatServiceImpl.java`

聊天 Mapper：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/mapper/chat/ChatSessionMapper.java`
- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/mapper/chat/ChatMessageMapper.java`

聊天 SQL：

- `zhuanzhuan-server/src/main/resources/mapper/chat/ChatSessionMapper.xml`
- `zhuanzhuan-server/src/main/resources/mapper/chat/ChatMessageMapper.xml`

聊天 WebSocket：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/websocket/chat/UserChatWebSocketEndpoint.java`

聊天相关 DTO / VO / Entity：

- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/dto/chat/...`
- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/vo/chat/...`
- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/entity/chat/...`

---

## 12. 下一步建议

前端现在可以直接开始做：

- 会话列表页
- 聊天详情页
- 全局聊天未读红点
- WebSocket 实时收消息

后端下一步建议继续补：

- 通知模块接口
- 聊天消息顺序优化
- 图片消息
- 会话分页
- 通知 WebSocket 事件

