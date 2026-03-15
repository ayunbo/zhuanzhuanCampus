-- ----------------------------
-- Virtual Wallet Simplified Tables
-- 外部独立钱包 App 模型
-- 目标：只支持钱包余额、银行卡余额、支付密码
-- 不修改现有 order / pay / pay_record 表
-- 钱包用户与校园平台 user 完全解耦
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for wallet_account
-- 钱包 App 自己的账户体系
-- ----------------------------
DROP TABLE IF EXISTS `wallet_account`;
CREATE TABLE `wallet_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `wallet_user_no` varchar(32) NOT NULL COMMENT '钱包用户号',
  `login_name` varchar(50) NOT NULL COMMENT '钱包登录名',
  `wallet_name` varchar(50) NOT NULL COMMENT '钱包昵称',
  `phone` varchar(20) NOT NULL COMMENT '钱包手机号',
  `pay_password` varchar(64) NOT NULL COMMENT '支付密码摘要',
  `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最近登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
  `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wallet_account_user_no` (`wallet_user_no`) USING BTREE,
  UNIQUE KEY `uk_wallet_account_login_name` (`login_name`) USING BTREE,
  UNIQUE KEY `uk_wallet_account_phone` (`phone`) USING BTREE,
  KEY `idx_wallet_account_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚拟钱包账户表' ROW_FORMAT=Dynamic;

-- ----------------------------
-- Table structure for wallet_bank_card
-- 一张银行卡由钱包绑定，并维护卡内可用余额
-- ----------------------------
DROP TABLE IF EXISTS `wallet_bank_card`;
CREATE TABLE `wallet_bank_card` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `wallet_account_id` bigint NOT NULL COMMENT '钱包账户id',
  `bank_name` varchar(50) NOT NULL COMMENT '银行名称',
  `card_holder` varchar(50) NOT NULL COMMENT '持卡人姓名',
  `card_no` varchar(64) NOT NULL COMMENT '银行卡号，可加密存储',
  `card_no_mask` varchar(32) NOT NULL COMMENT '银行卡掩码',
  `card_type` tinyint NOT NULL DEFAULT 1 COMMENT '卡类型：1借记卡 2信用卡',
  `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '银行卡余额',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认卡：0否 1是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0解绑 1正常',
  `bind_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑卡时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
  `update_user` bigint NOT NULL DEFAULT 0 COMMENT '修改人id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wallet_bank_card_no` (`card_no`) USING BTREE,
  KEY `idx_wallet_bank_card_account` (`wallet_account_id`) USING BTREE,
  KEY `idx_wallet_bank_card_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚拟钱包银行卡表' ROW_FORMAT=Dynamic;

-- ----------------------------
-- Seed demo data
-- 默认支付密码均为 123456 的 md5
-- ----------------------------
INSERT INTO `wallet_account`
(`wallet_user_no`, `login_name`, `wallet_name`, `phone`, `pay_password`, `balance`, `status`, `create_user`, `update_user`)
VALUES
('WALLET10001', 'wallet_alice', '测试钱包A', '18800001111', 'e10adc3949ba59abbe56e057f20f883e', 5000.00, 1, 1, 1),
('WALLET10002', 'wallet_bob', '测试钱包B', '18800002222', 'e10adc3949ba59abbe56e057f20f883e', 3000.00, 1, 2, 2);

INSERT INTO `wallet_bank_card`
(`wallet_account_id`, `bank_name`, `card_holder`, `card_no`, `card_no_mask`, `card_type`, `balance`, `is_default`, `status`, `create_user`, `update_user`)
VALUES
(1, '中国建设银行', '测试用户A', '6227000000000000001', '6227********0001', 1, 8000.00, 1, 1, 1, 1),
(1, '中国工商银行', '测试用户A', '6222000000000000001', '6222********0001', 1, 1500.00, 0, 1, 1, 1),
(2, '中国农业银行', '测试用户B', '6228000000000000002', '6228********0002', 1, 2600.00, 1, 1, 2, 2);

SET FOREIGN_KEY_CHECKS = 1;
