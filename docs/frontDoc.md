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

通知模块当前已经落地一个“基础查询原型”，目标是先让前端把 `notice` 表里的通知渲染进聊天页中的“通知消息”会话。

当前已经支持：

- 系统通知会话摘要
- 通知消息列表查询
- 通知未读总数
- 单条通知已读
- 全部通知已读

当前还没做：

- 业务事件自动生成通知
- 通知 WebSocket 实时推送
- 顶部胶囊弹窗
- 通知点击后的业务跳转编排

当前接口路径：

- `/user/notice/...`

前端展示约定：

- 后端返回的是通知数据，不是真实聊天消息
- 前端可以把这些通知渲染成“系统通知”会话里的气泡列表
- 如果当前只是做基础原型，前端可先只查询未读通知并展示到该会话里

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

## 5.3 `notice`

通知表，用来支撑系统通知消息。

关键字段：
- `id`
- `user_id`
- `type`
- `title`
- `content`
- `biz_type`
- `biz_id`
- `read_status`
- `read_time`
- `create_time`

前端重点理解：
- `notice` 不是聊天消息表，但可以被前端渲染成“系统通知”会话中的消息气泡
- 一条 `notice` 记录，对应前端一条系统通知气泡
- `title + content` 足够支撑系统通知气泡展示
- `read_status` 和 `read_time` 可直接决定已读态样式

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

## 7.7 通知原型接口

这一版通知模块的目标，不是把完整通知系统一次做完，而是先给前端一个可联调的“系统通知会话”数据源。

---

## 7.7.1 查询系统通知会话摘要

接口：
```http
GET /user/notice/session/summary
```

用途：

- 用于聊天页左侧会话列表中的“通知消息”卡片
- 展示系统通知会话名称、最后一条通知摘要、最后时间、未读数

成功返回示例：
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "sessionKey": "system-notice",
    "sessionName": "通知消息",
    "lastNoticeId": 970004,
    "lastTitle": "系统通知",
    "lastMsg": "系统通知 - 聊天模块测试数据已准备完成，你现在可以直接联调聊天页面。",
    "lastTime": "2026-03-17T08:30:00",
    "unreadCount": 2
  }
}
```

前端建议：

- 这个摘要对象不是 `chatsession`，而是前端虚拟系统会话的数据源
- 可以把它和普通聊天会话一起渲染在会话列表里

---

## 7.7.2 查询通知消息列表

接口：
```http
GET /user/notice/message/list?pageNo=1&pageSize=20&readStatus=0
```

说明：

- `readStatus` 可选
- `readStatus = 0` 表示只查未读通知
- `readStatus = 1` 表示只查已读通知
- 不传 `readStatus` 表示查全部通知

成功返回示例：
```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "id": 970004,
      "type": 4,
      "title": "系统通知",
      "content": "聊天模块测试数据已准备完成，你现在可以直接联调聊天页面。",
      "bizType": 4,
      "bizId": 950001,
      "readStatus": 0,
      "readTime": null,
      "createTime": "2026-03-17T08:30:00"
    }
  ]
}
```

前端建议：

- 后端按时间倒序返回
- 如果聊天页要按正常消息流从旧到新展示，可自行 `reverse`
- 对于基础原型，前端可以先固定请求 `readStatus=0`

---

## 7.7.3 查询通知未读总数

接口：
```http
GET /user/notice/unread/count
```

成功返回示例：
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "totalUnreadCount": 2
  }
}
```

用途：

- 聊天页左侧“通知消息”会话红点
- 全局消息中心红点

---

## 7.7.4 单条通知标记已读

接口：
```http
PATCH /user/notice/read
```

请求体：
```json
{
  "noticeId": 970004
}
```

适用场景：

- 点击一条通知后标记已读
- 某条通知进入可见状态后标记已读

---

## 7.7.5 全部通知标记已读

接口：
```http
POST /user/notice/read-all
```

适用场景：

- 用户进入“通知消息”会话后一次性清空未读
- 列表页提供“一键全部已读”

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

强约束：

- `/ws/chat` 是 WebSocket 端点，不是普通 HTTP 接口
- 前端必须使用浏览器原生 `WebSocket` 或等价的 WebSocket 客户端发起连接
- 页面为 `http` 时使用 `ws://`
- 页面为 `https` 时使用 `wss://`
- 不允许用 `axios`、`fetch`、`uni.request`、普通 `<a>` 跳转、浏览器地址栏直接访问等方式请求 `/ws/chat`
- 不允许把 `/ws/chat` 当成 REST API 做 GET/POST 联调

错误示例：

```js
fetch('http://localhost:8080/ws/chat?token=xxx')
axios.get('/ws/chat?token=xxx')
window.location.href = 'http://localhost:8080/ws/chat?token=xxx'
```

正确示例：

```js
const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
const socket = new WebSocket(`${protocol}://localhost:8080/ws/chat?token=${token}`)
```

联调验收标准：

- 浏览器开发者工具 Network 中，这条连接应出现在 `WS` 分类下
- 握手成功时状态码应为 `101 Switching Protocols`
- 如果后端日志出现 `No static resource ws/chat`，通常表示前端把 WebSocket 端点当成了普通 HTTP 请求
- 如果连接失败，先检查协议是否写成了 `http://` 而不是 `ws://` / `wss://`

## 8.2 当前后端会主动推送的事件

当前已经实现两类事件：

- `chat.message`
- `chat.unread`
- `chat.read`

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

## 8.5 `chat.read`

表示当前会话中，对方已经读到了你发出的若干条消息。

示例：
```json
{
  "event": "chat.read",
  "data": {
    "sessionId": 174176000001,
    "readerId": 5,
    "readTime": "2026-03-23T15:30:00",
    "messageIds": [174176000101, 174176000102]
  }
}
```

前端处理建议：
- 只处理当前登录用户作为“发送方”的消息
- 根据 `messageIds` 精确更新本地消息列表中的 `readStatus` 和 `readTime`
- 如果当前就在对应会话页，可直接把这些消息更新为已读态

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

## 10.3 当前通知模块是基础查询原型

当前通知模块已经可以联调，但要注意它还只是原型版本：

- 已支持 `/user/notice/...` 的 HTTP 查询和已读接口
- 已支持把通知渲染进聊天页中的“通知消息”会话
- 暂未支持通知 WebSocket 推送
- 暂未支持通知顶部胶囊弹窗
- 暂未支持业务事件自动生成通知

因此前端当前更适合的接入方式是：

- 页面进入时主动拉通知会话摘要
- 主动拉通知消息列表
- 主动拉未读总数
- 需要时调用单条已读或全部已读接口

---

## 10.4 Swagger 注解中文乱码

如果你在某些查看工具里看到个别注解说明乱码，那是终端显示问题，不影响接口实际调用和字段命名。

---

## 10.5 WebSocket 实现红线

前端实现聊天实时能力时，必须遵守下面这些规则：

- WebSocket 只负责“服务端主动推送”
- 当前一期发送消息仍然走 HTTP 接口，不要改成通过 WebSocket 发送聊天内容
- 建连地址固定为 `/ws/chat?token={JWT}`，token 放 query 参数，不放请求头
- 建连失败时不要自动降级成请求 `http://.../ws/chat`
- 重连时仍然必须按 WebSocket 协议重新握手，不能改成轮询这个地址
- 如果项目有代理层，也必须保证 WebSocket upgrade 正常转发，不能按普通 HTTP 转发

建议前端 AI 在输出代码前自检：

- 是否使用了 `new WebSocket(...)` 或明确的 WebSocket 客户端
- 是否使用了 `ws://` / `wss://`，而不是 `http://` / `https://`
- 是否误把 `/ws/chat` 写进了接口请求封装层
- 是否把“发送消息”错误地写成了 `websocket.send(...)`，而不是继续调用后端现有 HTTP 发消息接口

---

## 10.6 给前端 AI 的提示词模板

以后如果让 AI 实现聊天前端，建议在提示词里明确加上下面这段约束：

```text
本项目聊天模块有一个后端 WebSocket 端点：/ws/chat?token={JWT}。

你必须严格区分 WebSocket 端点和普通 HTTP 接口：
1. /ws/chat 只能使用 WebSocket 协议连接，必须写成 ws:// 或 wss://，不能写成 http:// 或 https://。
2. 只能使用 new WebSocket(...) 或等价 WebSocket 客户端连接，不能使用 fetch、axios、request、页面跳转或浏览器直接访问。
3. 发送聊天消息仍然调用现有 HTTP 接口，不要改成通过 websocket.send 发送业务消息。
4. 如果你要输出聊天连接代码，请同时给出连接、断开、重连、onmessage、onerror 的处理。
5. 输出代码前请自检：浏览器 Network 中该请求应属于 WS，成功握手状态应为 101 Switching Protocols。

如果你不确定某个地址是不是 WebSocket 端点，先不要把它当 REST 接口调用。
```

如果你想进一步减少 AI 跑偏，可以再补一句：

```text
凡是形如 /ws/... 的地址，默认先按 WebSocket 端点理解，除非我明确说明它是普通 HTTP 接口。
```

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

通知控制器：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/controller/user/notify/UserNoticeController.java`

通知业务：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/service/notify/NoticeService.java`
- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/service/impl/notify/NoticeServiceImpl.java`

通知 Mapper：

- `zhuanzhuan-server/src/main/java/com/zhuanzhuan/mapper/notify/NoticeMapper.java`

通知 SQL：

- `zhuanzhuan-server/src/main/resources/mapper/notify/NoticeMapper.xml`

通知相关 DTO / VO / Entity：

- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/dto/notify/...`
- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/vo/notify/...`
- `zhuanzhuan-pojo/src/main/java/com/zhuanzhuan/entity/notify/...`

---

## 12. 下一步建议

前端现在可以直接开始做：

- 会话列表页
- 聊天详情页
- 全局聊天未读红点
- WebSocket 实时收消息
- “通知消息”系统会话
- 通知消息气泡列表
- 通知未读红点

后端下一步建议继续补：

- 通知生成链路
- 通知 WebSocket 事件
- 顶部胶囊弹窗联动事件
- 聊天消息顺序优化
- 图片消息
- 会话分页
