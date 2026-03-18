-- 恢复 userId=6 聊天测试数据的未读状态
--
-- 适用前提：
-- 1. 你已执行过 sql/chat_notice_mock_data_user6.sql
-- 2. 会话与消息使用该脚本里的测试 ID（950xxx / 960xxx）
--
-- 作用：
-- 1. 把被你“读掉”的未读恢复回来
-- 2. 同步修正 chatsession 的 unread 计数，保证前端红点能正常验证

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- =========================================================
-- 会话 950001：seller_unread = 1, buyer_unread = 0
-- 逻辑：买家(6)最后一条消息发给卖家(94001)，卖家未读
-- =========================================================

UPDATE chatmessage
SET read_status = 0,
    read_time = NULL,
    update_time = NOW(),
    update_user = 6
WHERE id = 960003
  AND session_id = 950001;

UPDATE chatmessage
SET read_status = 1,
    read_time = COALESCE(read_time, NOW()),
    update_time = NOW()
WHERE id IN (960001, 960002)
  AND session_id = 950001;

UPDATE chatsession
SET seller_unread = 1,
    buyer_unread = 0,
    update_time = NOW(),
    update_user = 6
WHERE id = 950001;

-- =========================================================
-- 会话 950002：seller_unread = 0, buyer_unread = 2
-- 逻辑：卖家(94002)连续两条消息发给买家(6)，买家未读
-- =========================================================

UPDATE chatmessage
SET read_status = 0,
    read_time = NULL,
    update_time = NOW(),
    update_user = 94002
WHERE id IN (960005, 960006)
  AND session_id = 950002;

UPDATE chatmessage
SET read_status = 1,
    read_time = COALESCE(read_time, NOW()),
    update_time = NOW()
WHERE id = 960004
  AND session_id = 950002;

UPDATE chatsession
SET seller_unread = 0,
    buyer_unread = 2,
    update_time = NOW(),
    update_user = 94002
WHERE id = 950002;

-- =========================================================
-- 会话 950003：seller_unread = 0, buyer_unread = 0
-- 逻辑：这个会话作为“无未读”对照组
-- =========================================================

UPDATE chatmessage
SET read_status = 1,
    read_time = COALESCE(read_time, NOW()),
    update_time = NOW()
WHERE id IN (960007, 960008, 960009, 960010)
  AND session_id = 950003;

UPDATE chatsession
SET seller_unread = 0,
    buyer_unread = 0,
    update_time = NOW(),
    update_user = 6
WHERE id = 950003;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- 可选自查：
-- SELECT id, seller_unread, buyer_unread, last_msg, last_time
-- FROM chatsession
-- WHERE id IN (950001, 950002, 950003);
--
-- SELECT id, session_id, sender_id, receiver_id, read_status, read_time, content
-- FROM chatmessage
-- WHERE id IN (960001,960002,960003,960004,960005,960006,960007,960008,960009,960010)
-- ORDER BY session_id, id;
