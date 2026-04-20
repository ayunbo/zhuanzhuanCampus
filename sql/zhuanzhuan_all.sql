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

-- ----------------------------
-- Table structure for browsehistory
-- ----------------------------
DROP TABLE IF EXISTS `browsehistory`;
CREATE TABLE `browsehistory`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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

-- ----------------------------
-- Table structure for chatmessage
-- ----------------------------
DROP TABLE IF EXISTS `chatmessage`;
CREATE TABLE `chatmessage`  (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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

-- ----------------------------
-- Table structure for goodsimage
-- ----------------------------
DROP TABLE IF EXISTS `goodsimage`;
CREATE TABLE `goodsimage`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
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
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `admin_id` bigint NOT NULL COMMENT '操作管理员id',
                               `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员名称',
                               `operation_type` tinyint NOT NULL COMMENT '操作类型：1商品审核 2卖家认证审核 3举报处理',
                               `target_id` bigint NOT NULL COMMENT '操作对象id',
                               `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作动作，如通过/驳回/处理/忽略',
                               `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作详情/备注',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
                               `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_audit_log_admin`(`admin_id` ASC) USING BTREE,
                               INDEX `idx_audit_log_type_time`(`operation_type` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审核操作流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_log
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

SET FOREIGN_KEY_CHECKS = 1;
