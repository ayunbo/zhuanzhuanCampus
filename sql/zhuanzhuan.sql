/*
 Navicat Premium Data Transfer

 Source Server         : JAVA
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : zhuanzhuan

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 16/03/2026 08:45:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                          `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员账号',
                          `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                          `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员名称',
                          `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
                          `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 2禁用',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                          `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE,
                          INDEX `idx_admin_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'Aries', '111', 1, '2026-03-12 17:39:28', '2026-03-12 17:42:22', 0, 0);

-- ----------------------------
-- Table structure for browsehistory
-- ----------------------------
DROP TABLE IF EXISTS `browsehistory`;
CREATE TABLE `browsehistory`  (
                                  `id` bigint NOT NULL COMMENT '主键id',
                                  `user_id` bigint NOT NULL COMMENT '用户id，逻辑关联user.id',
                                  `goods_id` bigint NOT NULL COMMENT '商品id，逻辑关联goods.id',
                                  `browse_count` int NOT NULL DEFAULT 1 COMMENT '浏览次数',
                                  `last_browse_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后浏览时间',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                  `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `uk_browsehistory_user_goods`(`user_id` ASC, `goods_id` ASC) USING BTREE,
                                  INDEX `idx_browsehistory_time`(`last_browse_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浏览历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of browsehistory
-- ----------------------------

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
                             `id` bigint NOT NULL COMMENT '主键id',
                             `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类id',
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
                             `level` tinyint NOT NULL DEFAULT 1 COMMENT '层级：1/2/3',
                             `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
                             `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                             `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_category_parent_name`(`parent_id` ASC, `name` ASC) USING BTREE,
                             INDEX `idx_category_parent_status`(`parent_id` ASC, `status` ASC) USING BTREE,
                             INDEX `idx_category_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1001, 0, '教材', 1, 1, 1, '2026-03-12 16:13:11', '2026-03-12 16:13:11', 1, 1);

-- ----------------------------
-- Table structure for chatmessage
-- ----------------------------
DROP TABLE IF EXISTS `chatmessage`;
CREATE TABLE `chatmessage`  (
                                `id` bigint NOT NULL COMMENT '主键id',
                                `session_id` bigint NOT NULL COMMENT '会话id，逻辑关联chatsession.id',
                                `sender_id` bigint NOT NULL COMMENT '发送者id，逻辑关联user.id',
                                `receiver_id` bigint NOT NULL COMMENT '接收者id，逻辑关联user.id',
                                `type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1文本 2图片 3系统',
                                `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
                                `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '已读状态：0未读 1已读',
                                `read_time` datetime NULL DEFAULT NULL COMMENT '已读时间',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_chatmessage_session_time`(`session_id` ASC, `create_time` ASC) USING BTREE,
                                INDEX `idx_chatmessage_receiver_read`(`receiver_id` ASC, `read_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chatmessage
-- ----------------------------

-- ----------------------------
-- Table structure for chatsession
-- ----------------------------
DROP TABLE IF EXISTS `chatsession`;
CREATE TABLE `chatsession`  (
                                `id` bigint NOT NULL COMMENT '主键id',
                                `goods_id` bigint NOT NULL COMMENT '商品id，逻辑关联goods.id',
                                `seller_id` bigint NOT NULL COMMENT '卖家id，逻辑关联user.id',
                                `buyer_id` bigint NOT NULL COMMENT '买家id，逻辑关联user.id',
                                `goods_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品标题',
                                `goods_cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品封面',
                                `last_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息摘要',
                                `last_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
                                `seller_unread` int NOT NULL DEFAULT 0 COMMENT '卖家未读数',
                                `buyer_unread` int NOT NULL DEFAULT 0 COMMENT '买家未读数',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_chatsession_goods_users`(`goods_id` ASC, `seller_id` ASC, `buyer_id` ASC) USING BTREE,
                                INDEX `idx_chatsession_seller_time`(`seller_id` ASC, `last_time` ASC) USING BTREE,
                                INDEX `idx_chatsession_buyer_time`(`buyer_id` ASC, `last_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chatsession
-- ----------------------------

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
                             `id` bigint NOT NULL COMMENT '主键id',
                             `user_id` bigint NOT NULL COMMENT '用户id，逻辑关联user.id',
                             `goods_id` bigint NOT NULL COMMENT '商品id，逻辑关联goods.id',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                             `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_favorite_user_goods`(`user_id` ASC, `goods_id` ASC) USING BTREE,
                             INDEX `idx_favorite_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
                          `id` bigint NOT NULL COMMENT '主键id',
                          `seller_id` bigint NOT NULL COMMENT '卖家id，逻辑关联user.id',
                          `category_id` bigint NOT NULL COMMENT '分类id，逻辑关联category.id',
                          `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题',
                          `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情',
                          `price` decimal(10, 2) NOT NULL COMMENT '售价',
                          `old_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
                          `quality` tinyint NOT NULL DEFAULT 5 COMMENT '成色等级',
                          `location` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面交地点',
                          `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1待审 2驳回 3在售 4锁定 5已售出 6已下架',
                          `cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图地址',
                          `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
                          `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员id，逻辑关联admin.id',
                          `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
                          `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
                          `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
                          `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏量',
                          `lock_order_id` bigint NULL DEFAULT NULL COMMENT '锁定订单id',
                          `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                          `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                          PRIMARY KEY (`id`) USING BTREE,
                          INDEX `idx_goods_seller_status`(`seller_id` ASC, `status` ASC) USING BTREE,
                          INDEX `idx_goods_category_status`(`category_id` ASC, `status` ASC) USING BTREE,
                          INDEX `idx_goods_status_publish`(`status` ASC, `publish_time` ASC) USING BTREE,
                          INDEX `idx_goods_title`(`title` ASC) USING BTREE,
                          INDEX `idx_goods_audit_admin`(`audit_admin_id` ASC) USING BTREE,
                          INDEX `idx_goods_lock_order`(`lock_order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (10001, 2, 1001, '高等数学第七版', '九成新高数教材，带部分笔记', 25.00, 49.80, 3, '图书馆门口', 4, 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', NULL, NULL, NULL, '2026-03-12 16:13:25', 0, 0, 7, 7, '2026-03-12 16:13:25', '2026-03-12 23:21:48', 2, 2);
INSERT INTO `goods` VALUES (10002, 2, 1001, '大学英语教材', '八成新，无破损', 18.00, 35.00, 3, '一食堂门口', 4, 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', NULL, NULL, NULL, '2026-03-12 16:13:36', 0, 0, 6, 2, '2026-03-12 16:13:36', '2026-03-12 21:26:06', 2, 2);
INSERT INTO `goods` VALUES (10004, 2, 1001, '计算机网络教材', '内容完整，封面略旧', 22.00, 45.00, 3, '宿舍楼下', 3, 'https://via.placeholder.com/300x220?text=network', NULL, NULL, NULL, '2026-03-12 21:16:15', 9, 1, NULL, 3, '2026-03-12 21:16:15', '2026-03-14 19:51:11', 2, 2);
INSERT INTO `goods` VALUES (10005, 2, 1001, '考研数学真题册', '近十年真题，附少量笔记', 28.00, 59.00, 4, '图书馆门口', 4, 'https://via.placeholder.com/300x220?text=exam-math', NULL, NULL, NULL, '2026-03-12 21:16:15', 21, 7, 13, 1, '2026-03-12 21:16:15', '2026-03-14 20:53:14', 2, 2);
INSERT INTO `goods` VALUES (10006, 2, 1001, 'C语言程序设计', '大一上教材，保存较好', 16.00, 39.80, 4, '二食堂门口', 4, 'https://via.placeholder.com/300x220?text=c-language', NULL, NULL, NULL, '2026-03-12 21:16:15', 5, 0, 10, 5, '2026-03-12 21:16:15', '2026-03-14 19:40:15', 2, 2);
INSERT INTO `goods` VALUES (10007, 2, 1001, '操作系统教材', '有部分重点划线', 26.00, 52.00, 4, '实验楼门口', 4, 'https://via.placeholder.com/300x220?text=os', NULL, NULL, NULL, '2026-03-12 21:16:15', 11, 2, 9, 1, '2026-03-12 21:16:15', '2026-03-12 23:57:56', 2, 2);
INSERT INTO `goods` VALUES (10008, 2, 1001, '离散数学教材', '适合计科专业基础课使用', 20.00, 42.00, 4, '主教楼下', 5, 'https://via.placeholder.com/300x220?text=discrete-math', NULL, NULL, NULL, '2026-03-12 21:16:15', 6, 1, 8, 2, '2026-03-12 21:16:15', '2026-03-13 00:12:59', 2, 2);
INSERT INTO `goods` VALUES (10009, 2, 1001, '数据库系统概论', '九成新，附课堂总结', 24.00, 48.00, 4, '图书馆南门', 4, 'https://via.placeholder.com/300x220?text=database', NULL, NULL, NULL, '2026-03-12 21:16:15', 18, 4, 90001, 0, '2026-03-12 21:16:15', '2026-03-12 21:16:15', 2, 2);
INSERT INTO `goods` VALUES (10010, 2, 1001, '概率论与数理统计', '内容完整，无缺页', 19.00, 38.00, 4, '一食堂门口', 4, 'https://via.placeholder.com/300x220?text=probability', NULL, NULL, NULL, '2026-03-12 21:16:15', 13, 3, 90002, 0, '2026-03-12 21:16:15', '2026-03-12 21:16:15', 2, 2);
INSERT INTO `goods` VALUES (10011, 2, 1001, '编译原理教材', '较新，少量折角', 35.00, 68.00, 4, '教学楼B区', 5, 'https://via.placeholder.com/300x220?text=compiler', NULL, NULL, NULL, '2026-03-12 21:16:15', 20, 5, NULL, 0, '2026-03-12 21:16:15', '2026-03-12 21:16:15', 2, 2);
INSERT INTO `goods` VALUES (10012, 2, 1001, '线性代数教材', '已售出样例商品', 17.00, 36.00, 4, '宿舍楼下', 5, 'https://via.placeholder.com/300x220?text=linear-algebra', NULL, NULL, NULL, '2026-03-12 21:16:15', 7, 1, NULL, 0, '2026-03-12 21:16:15', '2026-03-12 21:16:15', 2, 2);

-- ----------------------------
-- Table structure for goodsimage
-- ----------------------------
DROP TABLE IF EXISTS `goodsimage`;
CREATE TABLE `goodsimage`  (
                               `id` bigint NOT NULL COMMENT '主键id',
                               `goods_id` bigint NOT NULL COMMENT '商品id，逻辑关联goods.id',
                               `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
                               `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
                               `is_cover` tinyint NOT NULL DEFAULT 0 COMMENT '是否封面：0否 1是',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                               `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_goodsimage_goods_sort`(`goods_id` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goodsimage
-- ----------------------------

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
                           `id` bigint NOT NULL COMMENT '主键id',
                           `user_id` bigint NOT NULL COMMENT '接收用户id，逻辑关联user.id',
                           `type` tinyint NOT NULL COMMENT '通知类型：1订单 2审核 3举报 4系统',
                           `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
                           `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
                           `biz_type` tinyint NULL DEFAULT NULL COMMENT '业务类型：1商品 2订单 3卖家认证 4聊天 5举报',
                           `biz_id` bigint NULL DEFAULT NULL COMMENT '业务主键id',
                           `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '读取状态：0未读 1已读',
                           `read_time` datetime NULL DEFAULT NULL COMMENT '读取时间',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                           `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                           PRIMARY KEY (`id`) USING BTREE,
                           INDEX `idx_notice_user_read`(`user_id` ASC, `read_status` ASC) USING BTREE,
                           INDEX `idx_notice_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                          `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                          `goods_id` bigint NOT NULL COMMENT '商品id',
                          `seller_id` bigint NOT NULL COMMENT '卖家id',
                          `buyer_id` bigint NOT NULL COMMENT '买家id',
                          `amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
                          `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付 1已支付 2已完成 3已取消 4超时关闭',
                          `expire_time` datetime NOT NULL COMMENT '支付过期时间',
                          `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                          `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
                          `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
                          `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                          `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                          INDEX `idx_order_buyer_status_time`(`buyer_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
                          INDEX `idx_order_seller_status_time`(`seller_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
                          INDEX `idx_order_goods`(`goods_id` ASC) USING BTREE,
                          INDEX `idx_order_status_expire`(`status` ASC, `expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (2, 'ORD1773318266493', 10001, 2, 1, 25.00, 1, '2026-03-12 20:54:26', '2026-03-12 20:24:33', NULL, NULL, 1, '2026-03-12 20:24:26', '2026-03-12 20:24:32', 1, 1);
INSERT INTO `order` VALUES (3, 'ORD1773318746352', 10002, 2, 3, 18.00, 4, '2026-03-12 21:02:26', NULL, '2026-03-12 21:03:00', NULL, 1, '2026-03-12 20:32:26', '2026-03-12 21:03:00', 3, 3);
INSERT INTO `order` VALUES (4, 'ORD1773320015340', 10001, 2, 3, 25.00, 4, '2026-03-12 21:23:35', NULL, '2026-03-12 21:24:00', NULL, 1, '2026-03-12 20:53:35', '2026-03-12 21:24:00', 3, 3);
INSERT INTO `order` VALUES (5, 'ORD1773320569255', 10001, 2, 3, 25.00, 4, '2026-03-12 21:32:49', NULL, '2026-03-12 21:33:00', NULL, 1, '2026-03-12 21:02:49', '2026-03-12 21:33:00', 3, 3);
INSERT INTO `order` VALUES (6, 'ORD1773321966615', 10002, 2, 3, 18.00, 1, '2026-03-12 21:56:07', '2026-03-12 21:26:11', NULL, NULL, 1, '2026-03-12 21:26:06', '2026-03-12 21:26:11', 3, 3);
INSERT INTO `order` VALUES (7, 'ORD1773328908248', 10001, 2, 3, 25.00, 1, '2026-03-12 23:51:48', '2026-03-12 23:21:50', NULL, NULL, 1, '2026-03-12 23:21:48', '2026-03-12 23:21:50', 3, 3);
INSERT INTO `order` VALUES (8, 'ORD1773330220330', 10008, 2, 3, 20.00, 2, '2026-03-13 00:13:40', '2026-03-12 23:49:54', NULL, '2026-03-13 00:12:59', 2, '2026-03-12 23:43:40', '2026-03-13 00:12:59', 3, 3);
INSERT INTO `order` VALUES (9, 'ORD1773331076065', 10007, 2, 3, 26.00, 1, '2026-03-13 00:27:56', '2026-03-12 23:59:14', NULL, NULL, 1, '2026-03-12 23:57:56', '2026-03-12 23:59:14', 3, 3);
INSERT INTO `order` VALUES (10, 'ORD1773332038531', 10006, 2, 3, 16.00, 1, '2026-03-13 00:43:59', '2026-03-14 19:40:23', NULL, NULL, 14, '2026-03-13 00:13:58', '2026-03-14 19:40:23', 3, 3);
INSERT INTO `order` VALUES (11, 'ORD1773487102161', 10006, 2, 3, 16.00, 1, '2026-03-14 19:48:22', '2026-03-14 19:25:44', '2026-03-14 19:21:50', NULL, 3, '2026-03-14 19:18:22', '2026-03-14 19:25:43', 3, 3);
INSERT INTO `order` VALUES (12, 'ORD1773487335094', 10004, 2, 3, 22.00, 4, '2026-03-14 19:52:15', NULL, '2026-03-14 19:51:12', NULL, 1, '2026-03-14 19:22:15', '2026-03-14 19:51:11', 3, 3);
INSERT INTO `order` VALUES (13, 'ORD1773492794272', 10005, 2, 3, 28.00, 1, '2026-03-14 21:23:14', '2026-03-14 20:54:33', NULL, NULL, 1, '2026-03-14 20:53:14', '2026-03-14 20:54:33', 3, 3);

-- ----------------------------
-- Table structure for order_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `order_snapshot`;
CREATE TABLE `order_snapshot`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                   `order_id` bigint NOT NULL COMMENT '订单id',
                                   `goods_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题快照',
                                   `goods_cover` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品封面快照',
                                   `goods_price` decimal(10, 2) NOT NULL COMMENT '商品价格快照',
                                   `seller_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖家昵称快照',
                                   `seller_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖家手机号快照',
                                   `buyer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '买家昵称快照',
                                   `buyer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '买家手机号快照',
                                   `meet_location` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '面交地点',
                                   `meet_time` datetime NULL DEFAULT NULL COMMENT '面交时间',
                                   `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                   `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uk_order_snapshot_order`(`order_id` ASC) USING BTREE,
                                   INDEX `idx_order_snapshot_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单快照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_snapshot
-- ----------------------------
INSERT INTO `order_snapshot` VALUES (2, 2, '高等数学第七版', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 25.00, '卖家李四', '13800000002', '买家张三', '13800000001', '一食堂门口', '2026-03-02 23:24:00', '2', '2026-03-12 20:24:26', '2026-03-12 20:24:26', 1, 1);
INSERT INTO `order_snapshot` VALUES (3, 3, '大学英语教材', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 18.00, '卖家李四', '13800000002', '20231739', NULL, '一食堂门口', '2026-02-27 20:32:00', '2222', '2026-03-12 20:32:26', '2026-03-12 20:32:26', 3, 3);
INSERT INTO `order_snapshot` VALUES (4, 4, '高等数学第七版', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 25.00, '卖家李四', '13800000002', '20231739', NULL, '一食堂门口', '2026-03-10 20:53:00', '', '2026-03-12 20:53:35', '2026-03-12 20:53:35', 3, 3);
INSERT INTO `order_snapshot` VALUES (5, 5, '高等数学第七版', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 25.00, '卖家李四', '13800000002', '20231739', NULL, '教学楼A区', '2026-03-15 21:02:00', '', '2026-03-12 21:02:49', '2026-03-12 21:02:49', 3, 3);
INSERT INTO `order_snapshot` VALUES (6, 6, '大学英语教材', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 18.00, '卖家李四', '13800000002', '20231739', NULL, '一食堂门口', '2026-03-14 21:25:00', '', '2026-03-12 21:26:06', '2026-03-12 21:26:06', 3, 3);
INSERT INTO `order_snapshot` VALUES (7, 7, '高等数学第七版', 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 25.00, '卖家李四', '13800000002', '20231739', NULL, '教学楼A区', '2026-03-14 23:22:00', '2', '2026-03-12 23:21:48', '2026-03-12 23:21:48', 3, 3);
INSERT INTO `order_snapshot` VALUES (8, 8, '离散数学教材', 'https://via.placeholder.com/300x220?text=discrete-math', 20.00, '卖家李四', '13800000002', '20231739', NULL, '教学楼A区', '2026-03-14 23:44:00', '', '2026-03-12 23:43:40', '2026-03-12 23:43:40', 3, 3);
INSERT INTO `order_snapshot` VALUES (9, 9, '操作系统教材', 'https://via.placeholder.com/300x220?text=os', 26.00, '卖家李四', '13800000002', '含笑Aries', NULL, '宿舍楼下', '2026-04-12 23:58:00', '1', '2026-03-12 23:57:56', '2026-03-12 23:57:56', 3, 3);
INSERT INTO `order_snapshot` VALUES (10, 10, 'C语言程序设计', 'https://via.placeholder.com/300x220?text=c-language', 16.00, '卖家李四', '13800000002', '含笑Aries', NULL, '一食堂门口', '2026-03-16 00:14:00', '12', '2026-03-13 00:13:58', '2026-03-14 16:30:34', 3, 3);
INSERT INTO `order_snapshot` VALUES (11, 11, 'C语言程序设计', 'https://via.placeholder.com/300x220?text=c-language', 16.00, '卖家李四', '13800000002', '含笑Aries', NULL, '一食堂门口', '2026-03-14 19:19:00', '', '2026-03-14 19:18:22', '2026-03-14 19:18:22', 3, 3);
INSERT INTO `order_snapshot` VALUES (12, 12, '计算机网络教材', 'https://via.placeholder.com/300x220?text=network', 22.00, '卖家李四', '13800000002', '含笑Aries', NULL, '一食堂门口', '2026-03-16 19:23:00', '', '2026-03-14 19:22:15', '2026-03-14 19:22:15', 3, 3);
INSERT INTO `order_snapshot` VALUES (13, 13, '考研数学真题册', 'https://via.placeholder.com/300x220?text=exam-math', 28.00, '卖家李四', '13800000002', '含笑Aries', NULL, '图书馆门口', '2026-03-16 20:54:00', '', '2026-03-14 20:53:14', '2026-03-14 20:53:14', 3, 3);

-- ----------------------------
-- Table structure for pay
-- ----------------------------
DROP TABLE IF EXISTS `pay`;
CREATE TABLE `pay`  (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                        `pay_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
                        `request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付请求号，幂等控制',
                        `order_id` bigint NOT NULL COMMENT '订单id',
                        `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
                        `method` tinyint NOT NULL DEFAULT 1 COMMENT '支付方式：1模拟支付',
                        `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0待支付 1成功 2失败 3关闭',
                        `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                        `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                        `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                        PRIMARY KEY (`id`) USING BTREE,
                        UNIQUE INDEX `uk_pay_no`(`pay_no` ASC) USING BTREE,
                        UNIQUE INDEX `uk_pay_request_no`(`request_no` ASC) USING BTREE,
                        UNIQUE INDEX `uk_pay_order_id`(`order_id` ASC) USING BTREE,
                        INDEX `idx_pay_status_time`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay
-- ----------------------------
INSERT INTO `pay` VALUES (2, 'PAY1773318266505', 'REQ1773318266505', 2, 25.00, 1, 3, '2026-03-12 20:24:33', 1, '2026-03-12 20:24:26', '2026-03-12 20:53:11', 1, 1);
INSERT INTO `pay` VALUES (3, 'PAY1773318746372', 'REQ1773318746372', 3, 18.00, 1, 3, NULL, 0, '2026-03-12 20:32:26', '2026-03-12 20:52:08', 3, 3);
INSERT INTO `pay` VALUES (4, 'PAY1773320015360', 'REQ1773320015360', 4, 25.00, 1, 3, NULL, 1, '2026-03-12 20:53:35', '2026-03-12 21:24:00', 3, 3);
INSERT INTO `pay` VALUES (5, 'PAY1773320569263', 'REQ1773320569263', 5, 25.00, 1, 3, NULL, 1, '2026-03-12 21:02:49', '2026-03-12 21:33:00', 3, 3);
INSERT INTO `pay` VALUES (6, 'PAY1773321966634', 'REQ1773321966634', 6, 18.00, 1, 1, '2026-03-12 21:26:11', 1, '2026-03-12 21:26:06', '2026-03-12 21:26:11', 3, 3);
INSERT INTO `pay` VALUES (7, 'PAY1773328908282', 'REQ1773328908282', 7, 25.00, 1, 1, '2026-03-12 23:21:50', 1, '2026-03-12 23:21:48', '2026-03-12 23:21:50', 3, 3);
INSERT INTO `pay` VALUES (8, 'PAY1773330220355', 'REQ1773330220355', 8, 20.00, 1, 1, '2026-03-12 23:49:54', 1, '2026-03-12 23:43:40', '2026-03-12 23:49:54', 3, 3);
INSERT INTO `pay` VALUES (9, 'PAY1773331076091', 'REQ1773331076092', 9, 26.00, 1, 1, '2026-03-12 23:59:14', 1, '2026-03-12 23:57:56', '2026-03-12 23:59:14', 3, 3);
INSERT INTO `pay` VALUES (10, 'PAY1773332038548', 'REQ1773332038548', 10, 16.00, 1, 1, '2026-03-14 19:40:23', 3, '2026-03-13 00:13:58', '2026-03-14 19:40:23', 3, 3);
INSERT INTO `pay` VALUES (11, 'PAY1773487102177', 'REQ1773487102178', 11, 16.00, 1, 1, '2026-03-14 19:25:44', 1, '2026-03-14 19:18:22', '2026-03-14 19:25:43', 3, 3);
INSERT INTO `pay` VALUES (12, 'PAY1773487335101', 'REQ1773487335101', 12, 22.00, 1, 3, NULL, 3, '2026-03-14 19:22:15', '2026-03-14 19:51:11', 3, 3);
INSERT INTO `pay` VALUES (13, 'PAY1773492794358', 'VW177349286900513', 13, 28.00, 2, 1, '2026-03-14 20:54:33', 4, '2026-03-14 20:53:14', '2026-03-14 20:54:33', 3, 3);

-- ----------------------------
-- Table structure for pay_record
-- ----------------------------
DROP TABLE IF EXISTS `pay_record`;
CREATE TABLE `pay_record`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `pay_id` bigint NOT NULL COMMENT '支付主表id',
                               `order_id` bigint NOT NULL COMMENT '订单id',
                               `record_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付流水号',
                               `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '记录内容',
                               `status` tinyint NOT NULL COMMENT '记录状态：0创建 1成功 2失败 3关闭',
                               `channel_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付渠道返回内容/备注',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                               `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_pay_record_no`(`record_no` ASC) USING BTREE,
                               INDEX `idx_pay_record_pay_id`(`pay_id` ASC) USING BTREE,
                               INDEX `idx_pay_record_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_record
-- ----------------------------
INSERT INTO `pay_record` VALUES (3, 2, 2, 'PR1773318266509', '创建支付单', 0, '模拟支付初始化', '2026-03-12 20:24:26', '2026-03-12 20:24:26', 1, 1);
INSERT INTO `pay_record` VALUES (4, 2, 2, 'PR1773318272919', '模拟支付成功', 1, '模拟支付完成', '2026-03-12 20:24:32', '2026-03-12 20:24:32', 1, 1);
INSERT INTO `pay_record` VALUES (5, 3, 3, 'PR1773318746377', '创建支付单', 0, '模拟支付初始化', '2026-03-12 20:32:26', '2026-03-12 20:32:26', 3, 3);
INSERT INTO `pay_record` VALUES (6, 4, 4, 'PR1773320015365', '创建支付单', 0, '模拟支付初始化', '2026-03-12 20:53:35', '2026-03-12 20:53:35', 3, 3);
INSERT INTO `pay_record` VALUES (7, 5, 5, 'PR1773320569266', '创建支付单', 0, '模拟支付初始化', '2026-03-12 21:02:49', '2026-03-12 21:02:49', 3, 3);
INSERT INTO `pay_record` VALUES (8, 3, 3, 'PR1773320581761', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-12 21:03:01', '2026-03-12 21:03:01', 0, 0);
INSERT INTO `pay_record` VALUES (9, 4, 4, 'PR1773321840057', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-12 21:24:00', '2026-03-12 21:24:00', 0, 0);
INSERT INTO `pay_record` VALUES (10, 6, 6, 'PR1773321966638', '创建支付单', 0, '模拟支付初始化', '2026-03-12 21:26:06', '2026-03-12 21:26:06', 3, 3);
INSERT INTO `pay_record` VALUES (11, 6, 6, 'PR1773321971332', '模拟支付成功', 1, '模拟支付完成', '2026-03-12 21:26:11', '2026-03-12 21:26:11', 3, 3);
INSERT INTO `pay_record` VALUES (12, 5, 5, 'PR1773322380054', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-12 21:33:00', '2026-03-12 21:33:00', 0, 0);
INSERT INTO `pay_record` VALUES (13, 7, 7, 'PR1773328908287', '创建支付单', 0, '模拟支付初始化', '2026-03-12 23:21:48', '2026-03-12 23:21:48', 3, 3);
INSERT INTO `pay_record` VALUES (14, 7, 7, 'PR1773328910390', '模拟支付成功', 1, '模拟支付完成', '2026-03-12 23:21:50', '2026-03-12 23:21:50', 3, 3);
INSERT INTO `pay_record` VALUES (15, 8, 8, 'PR1773330220359', '创建支付单', 0, '模拟支付初始化', '2026-03-12 23:43:40', '2026-03-12 23:43:40', 3, 3);
INSERT INTO `pay_record` VALUES (16, 8, 8, 'PR1773330594266', '模拟支付成功', 1, '模拟支付完成', '2026-03-12 23:49:54', '2026-03-12 23:49:54', 3, 3);
INSERT INTO `pay_record` VALUES (17, 9, 9, 'PR1773331076096', '创建支付单', 0, '模拟支付初始化', '2026-03-12 23:57:56', '2026-03-12 23:57:56', 3, 3);
INSERT INTO `pay_record` VALUES (18, 9, 9, 'PR1773331154063', '模拟支付成功', 1, '模拟支付完成', '2026-03-12 23:59:14', '2026-03-12 23:59:14', 3, 3);
INSERT INTO `pay_record` VALUES (19, 10, 10, 'PR1773332038553', '创建支付单', 0, '模拟支付初始化', '2026-03-13 00:13:58', '2026-03-13 00:13:58', 3, 3);
INSERT INTO `pay_record` VALUES (20, 10, 10, 'PR1773332047242', '用户取消订单，关闭支付单', 3, '用户主动取消订单', '2026-03-13 00:14:07', '2026-03-13 00:14:07', 3, 3);
INSERT INTO `pay_record` VALUES (21, 10, 10, 'PR1773477060050', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 16:31:00', '2026-03-14 16:31:00', 0, 0);
INSERT INTO `pay_record` VALUES (22, 10, 10, 'PR1773477240019', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 16:34:00', '2026-03-14 16:34:00', 0, 0);
INSERT INTO `pay_record` VALUES (23, 10, 10, 'PR1773478620043', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 16:57:00', '2026-03-14 16:57:00', 0, 0);
INSERT INTO `pay_record` VALUES (24, 10, 10, 'PR1773478860028', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 17:01:00', '2026-03-14 17:01:00', 0, 0);
INSERT INTO `pay_record` VALUES (25, 10, 10, 'PR1773485940058', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 18:59:00', '2026-03-14 18:59:00', 0, 0);
INSERT INTO `pay_record` VALUES (26, 10, 10, 'PR1773486000035', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:00:00', '2026-03-14 19:00:00', 0, 0);
INSERT INTO `pay_record` VALUES (27, 10, 10, 'PR1773486120203', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:02:00', '2026-03-14 19:02:00', 0, 0);
INSERT INTO `pay_record` VALUES (28, 10, 10, 'PR1773486660025', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:11:00', '2026-03-14 19:11:00', 0, 0);
INSERT INTO `pay_record` VALUES (29, 10, 10, 'PR1773486720023', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:12:00', '2026-03-14 19:12:00', 0, 0);
INSERT INTO `pay_record` VALUES (30, 10, 10, 'PR1773486840022', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:14:00', '2026-03-14 19:14:00', 0, 0);
INSERT INTO `pay_record` VALUES (31, 10, 10, 'PR1773486900035', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:15:00', '2026-03-14 19:15:00', 0, 0);
INSERT INTO `pay_record` VALUES (32, 11, 11, 'PR1773487102180', '创建支付单', 0, '模拟支付初始化', '2026-03-14 19:18:22', '2026-03-14 19:18:22', 3, 3);
INSERT INTO `pay_record` VALUES (33, 11, 11, 'PR1773487104798', '模拟支付成功', 1, '模拟支付完成', '2026-03-14 19:18:24', '2026-03-14 19:18:24', 3, 3);
INSERT INTO `pay_record` VALUES (34, 10, 10, 'PR1773487202469', '超时未支付，系统自动关闭支付单', 3, '系统定时任务自动关闭', '2026-03-14 19:20:02', '2026-03-14 19:20:02', 0, 0);
INSERT INTO `pay_record` VALUES (35, 11, 11, 'PR1773487310022', '用户取消订单，关闭支付单', 3, '用户主动取消订单', '2026-03-14 19:21:50', '2026-03-14 19:21:50', 3, 3);
INSERT INTO `pay_record` VALUES (36, 12, 12, 'PR1773487335102', '创建支付单', 0, '模拟支付初始化', '2026-03-14 19:22:15', '2026-03-14 19:22:15', 3, 3);
INSERT INTO `pay_record` VALUES (37, 12, 12, 'PR1773487350787', '模拟支付成功', 1, '模拟支付完成', '2026-03-14 19:22:30', '2026-03-14 19:22:30', 3, 3);
INSERT INTO `pay_record` VALUES (38, 10, 10, 'PR1773487547765', '管理员修改订单状态为关闭/取消', 3, '管理员后台操作', '2026-03-14 19:25:47', '2026-03-14 19:25:47', 0, 0);
INSERT INTO `pay_record` VALUES (39, 10, 10, 'PR1773488415472', '管理员修改订单状态', 0, 'adminChangeOrderStatus=0', '2026-03-14 19:40:15', '2026-03-14 19:40:15', 0, 0);
INSERT INTO `pay_record` VALUES (40, 10, 10, 'PR1773488423352', '模拟支付成功', 1, '模拟支付完成', '2026-03-14 19:40:23', '2026-03-14 19:40:23', 3, 3);
INSERT INTO `pay_record` VALUES (41, 12, 12, 'PR1773488430809', '管理员修改订单状态', 0, 'adminChangeOrderStatus=0', '2026-03-14 19:40:30', '2026-03-14 19:40:30', 0, 0);
INSERT INTO `pay_record` VALUES (42, 12, 12, 'PR1773489071540', '管理员修改订单状态', 3, 'adminChangeOrderStatus=4', '2026-03-14 19:51:11', '2026-03-14 19:51:11', 0, 0);
INSERT INTO `pay_record` VALUES (43, 13, 13, 'PR1773492794362', '创建支付单', 0, '模拟支付初始化', '2026-03-14 20:53:14', '2026-03-14 20:53:14', 3, 3);
INSERT INTO `pay_record` VALUES (44, 13, 13, 'PR1773492873160', '银行卡支付成功:6227********0001', 1, 'walletUserNo=WALLET10001', '2026-03-14 20:54:33', '2026-03-14 20:54:33', 3, 3);

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
                           `id` bigint NOT NULL COMMENT '主键id',
                           `report_user_id` bigint NOT NULL COMMENT '举报人id，逻辑关联user.id',
                           `target_type` tinyint NOT NULL COMMENT '举报对象类型：1商品 2用户 3消息',
                           `target_id` bigint NOT NULL COMMENT '举报对象id',
                           `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因',
                           `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1已处理 2已忽略',
                           `handle_admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员id，逻辑关联admin.id',
                           `handle_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理结果',
                           `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                           `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                           PRIMARY KEY (`id`) USING BTREE,
                           INDEX `idx_report_user`(`report_user_id` ASC) USING BTREE,
                           INDEX `idx_report_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
                           INDEX `idx_report_status_time`(`status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review`  (
                           `id` bigint NOT NULL COMMENT '主键id',
                           `order_id` bigint NOT NULL COMMENT '订单id，逻辑关联order.id',
                           `goods_id` bigint NOT NULL COMMENT '商品id，逻辑关联goods.id',
                           `user_id` bigint NOT NULL COMMENT '评价人id，逻辑关联user.id',
                           `target_user_id` bigint NOT NULL COMMENT '被评价人id，逻辑关联user.id',
                           `score` tinyint NOT NULL COMMENT '评分：1~5',
                           `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价内容',
                           `images` varchar(2048) NULL DEFAULT NULL COMMENT '图片链接，review image urls' ,
                           `anonymous` tinyint NOT NULL DEFAULT 0 COMMENT '是否匿名：0否 1是',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                           `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `uk_review_order`(`order_id` ASC) USING BTREE,
                           INDEX `idx_review_target_user`(`target_user_id` ASC) USING BTREE,
                           INDEX `idx_review_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review
-- ----------------------------

-- ----------------------------
-- Table structure for sellerauth
-- ----------------------------
DROP TABLE IF EXISTS `sellerauth`;
CREATE TABLE `sellerauth`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `user_id` bigint NOT NULL COMMENT '用户id，逻辑关联user.id',
                               `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
                               `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号',
                               `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
                               `material` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证材料地址',
                               `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待审 1通过 2驳回 3撤回',
                               `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
                               `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员id，逻辑关联admin.id',
                               `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                               `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_sellerauth_user`(`user_id` ASC) USING BTREE,
                               INDEX `idx_sellerauth_status_time`(`status` ASC, `create_time` ASC) USING BTREE,
                               INDEX `idx_sellerauth_admin`(`audit_admin_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '卖家认证表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sellerauth
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                         `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号（登录主账号）',
                         `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                         `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
                         `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（可选，绑定后可登录）',
                         `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
                         `role` tinyint NOT NULL DEFAULT 1 COMMENT '角色：1普通用户 2卖家',
                         `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 2封禁',
                         `campus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区',
                         `intro` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
                         `score_avg` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '平均评分',
                         `review_count` int NOT NULL DEFAULT 0 COMMENT '评价次数',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                         `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_user_student_no`(`student_no` ASC) USING BTREE,
                         UNIQUE INDEX `uk_user_phone`(`phone` ASC) USING BTREE,
                         INDEX `idx_user_role_status`(`role` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '20230001', '123456', '买家张三', '13800000001', NULL, 1, 1, '主校区', '我是买家', 0.00, 0, '2026-03-12 16:12:55', '2026-03-12 16:12:55', 1, 1);
INSERT INTO `user` VALUES (2, '20230002', '123456', '卖家李四', '13800000002', NULL, 2, 1, '主校区', '我是卖家', 5.00, 10, '2026-03-12 16:12:55', '2026-03-12 16:12:55', 2, 2);
INSERT INTO `user` VALUES (3, '20231739', 'e10adc3949ba59abbe56e057f20f883e', '含笑Aries', NULL, 'https://zhuanzhuancampus.oss-cn-beijing.aliyuncs.com/user/avatar/2026/03/12/374d187156754ef6ba8273044c3989ce.jpg', 1, 1, NULL, NULL, 0.00, 0, '2026-03-12 17:46:48', '2026-03-12 23:50:12', 0, 3);

-- ----------------------------
-- Table structure for wallet_account
-- ----------------------------
DROP TABLE IF EXISTS `wallet_account`;
CREATE TABLE `wallet_account`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                   `wallet_user_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包用户号',
                                   `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包登录名',
                                   `wallet_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包昵称',
                                   `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包手机号',
                                   `pay_password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付密码摘要',
                                   `balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
                                   `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
                                   `last_login_time` datetime NULL DEFAULT NULL COMMENT '最近登录时间',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                   `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uk_wallet_account_user_no`(`wallet_user_no` ASC) USING BTREE,
                                   UNIQUE INDEX `uk_wallet_account_login_name`(`login_name` ASC) USING BTREE,
                                   UNIQUE INDEX `uk_wallet_account_phone`(`phone` ASC) USING BTREE,
                                   INDEX `idx_wallet_account_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '虚拟钱包账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wallet_account
-- ----------------------------
INSERT INTO `wallet_account` VALUES (1, 'WALLET10001', 'wallet_alice', '测试钱包A', '18800001111', 'e10adc3949ba59abbe56e057f20f883e', 5000.00, 1, NULL, '2026-03-14 18:14:01', '2026-03-14 18:14:01', 1, 1);
INSERT INTO `wallet_account` VALUES (2, 'WALLET10002', 'wallet_bob', '测试钱包B', '18800002222', 'e10adc3949ba59abbe56e057f20f883e', 3000.00, 1, NULL, '2026-03-14 18:14:01', '2026-03-14 18:14:01', 2, 2);

-- ----------------------------
-- Table structure for wallet_bank_card
-- ----------------------------
DROP TABLE IF EXISTS `wallet_bank_card`;
CREATE TABLE `wallet_bank_card`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                     `wallet_account_id` bigint NOT NULL COMMENT '钱包账户id',
                                     `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '银行名称',
                                     `card_holder` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '持卡人姓名',
                                     `card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '银行卡号，可加密存储',
                                     `card_no_mask` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '银行卡掩码',
                                     `card_type` tinyint NOT NULL DEFAULT 1 COMMENT '卡类型：1借记卡 2信用卡',
                                     `balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '银行卡余额',
                                     `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认卡：0否 1是',
                                     `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0解绑 1正常',
                                     `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑卡时间',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                                     `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_wallet_bank_card_no`(`card_no` ASC) USING BTREE,
                                     INDEX `idx_wallet_bank_card_account`(`wallet_account_id` ASC) USING BTREE,
                                     INDEX `idx_wallet_bank_card_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '虚拟钱包银行卡表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wallet_bank_card
-- ----------------------------
INSERT INTO `wallet_bank_card` VALUES (1, 1, '中国建设银行', '测试用户A', '6227000000000000001', '6227********0001', 1, 7972.00, 1, 1, '2026-03-14 18:14:02', '2026-03-14 18:14:02', '2026-03-14 20:54:33', 1, 1);
INSERT INTO `wallet_bank_card` VALUES (2, 1, '中国工商银行', '测试用户A', '6222000000000000001', '6222********0001', 1, 1500.00, 0, 1, '2026-03-14 18:14:02', '2026-03-14 18:14:02', '2026-03-14 18:14:02', 1, 1);
INSERT INTO `wallet_bank_card` VALUES (3, 2, '中国农业银行', '测试用户B', '6228000000000000002', '6228********0002', 1, 2600.00, 1, 1, '2026-03-14 18:14:02', '2026-03-14 18:14:02', '2026-03-14 18:14:02', 2, 2);

SET FOREIGN_KEY_CHECKS = 1;
