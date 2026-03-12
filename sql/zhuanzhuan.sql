CREATE DATABASE IF NOT EXISTS zhuanzhuan
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE zhuanzhuan;

-- 1. 管理员表
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `username` VARCHAR(50) NOT NULL COMMENT '管理员账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) NOT NULL COMMENT '管理员名称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`),
    KEY `idx_admin_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `student_no` VARCHAR(50) NOT NULL COMMENT '学号（登录主账号）',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（可选，绑定后可登录）',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
    `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1普通用户 2卖家',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2封禁',
    `campus` VARCHAR(50) DEFAULT NULL COMMENT '校区',
    `intro` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `score_avg` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均评分',
    `review_count` INT NOT NULL DEFAULT 0 COMMENT '评价次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_student_no` (`student_no`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    KEY `idx_user_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
-- 3. 卖家认证表
CREATE TABLE `sellerauth` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id` BIGINT NOT NULL COMMENT '用户id，逻辑关联user.id',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `student_no` VARCHAR(30) NOT NULL COMMENT '学号',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `material` VARCHAR(512) DEFAULT NULL COMMENT '认证材料地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审 1通过 2驳回 3撤回',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员id，逻辑关联admin.id',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_sellerauth_user` (`user_id`),
    KEY `idx_sellerauth_status_time` (`status`, `create_time`),
    KEY `idx_sellerauth_admin` (`audit_admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卖家认证表';


-- 4. 分类表
CREATE TABLE `category` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类id',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级：1/2/3',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_parent_name` (`parent_id`, `name`),
    KEY `idx_category_parent_status` (`parent_id`, `status`),
    KEY `idx_category_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';


-- 5. 商品表
CREATE TABLE `goods` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `seller_id` BIGINT NOT NULL COMMENT '卖家id，逻辑关联user.id',
    `category_id` BIGINT NOT NULL COMMENT '分类id，逻辑关联category.id',
    `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
    `detail` TEXT COMMENT '商品详情',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `old_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `quality` TINYINT NOT NULL DEFAULT 5 COMMENT '成色等级',
    `location` VARCHAR(120) DEFAULT NULL COMMENT '面交地点',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1待审 2驳回 3在售 4锁定 5已售出 6已下架',
    `cover` VARCHAR(512) DEFAULT NULL COMMENT '封面图地址',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
    `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员id，逻辑关联admin.id',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏量',
    `lock_order_id` BIGINT DEFAULT NULL COMMENT '锁定订单id',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_goods_seller_status` (`seller_id`, `status`),
    KEY `idx_goods_category_status` (`category_id`, `status`),
    KEY `idx_goods_status_publish` (`status`, `publish_time`),
    KEY `idx_goods_title` (`title`),
    KEY `idx_goods_audit_admin` (`audit_admin_id`),
    KEY `idx_goods_lock_order` (`lock_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';


-- 6. 商品图片表
CREATE TABLE `goodsimage` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `url` VARCHAR(512) NOT NULL COMMENT '图片地址',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `is_cover` TINYINT NOT NULL DEFAULT 0 COMMENT '是否封面：0否 1是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_goodsimage_goods_sort` (`goods_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';


-- 7. 浏览历史表
CREATE TABLE `browsehistory` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `user_id` BIGINT NOT NULL COMMENT '用户id，逻辑关联user.id',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `browse_count` INT NOT NULL DEFAULT 1 COMMENT '浏览次数',
    `last_browse_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后浏览时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_browsehistory_user_goods` (`user_id`, `goods_id`),
    KEY `idx_browsehistory_time` (`last_browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';


-- 8. 收藏表
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `user_id` BIGINT NOT NULL COMMENT '用户id，逻辑关联user.id',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`),
    KEY `idx_favorite_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';


-- 9. 评价表
CREATE TABLE `review` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `order_id` BIGINT NOT NULL COMMENT '订单id，逻辑关联order.id',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `user_id` BIGINT NOT NULL COMMENT '评价人id，逻辑关联user.id',
    `target_user_id` BIGINT NOT NULL COMMENT '被评价人id，逻辑关联user.id',
    `score` TINYINT NOT NULL COMMENT '评分：1~5',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名：0否 1是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_order` (`order_id`),
    KEY `idx_review_target_user` (`target_user_id`),
    KEY `idx_review_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';


-- 10. 举报表
CREATE TABLE `report` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `report_user_id` BIGINT NOT NULL COMMENT '举报人id，逻辑关联user.id',
    `target_type` TINYINT NOT NULL COMMENT '举报对象类型：1商品 2用户 3消息',
    `target_id` BIGINT NOT NULL COMMENT '举报对象id',
    `reason` VARCHAR(255) NOT NULL COMMENT '举报原因',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1已处理 2已忽略',
    `handle_admin_id` BIGINT DEFAULT NULL COMMENT '处理管理员id，逻辑关联admin.id',
    `handle_result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_report_user` (`report_user_id`),
    KEY `idx_report_target` (`target_type`, `target_id`),
    KEY `idx_report_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';


-- 11. 通知表
CREATE TABLE `notice` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `user_id` BIGINT NOT NULL COMMENT '接收用户id，逻辑关联user.id',
    `type` TINYINT NOT NULL COMMENT '通知类型：1订单 2审核 3举报 4系统',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` VARCHAR(500) NOT NULL COMMENT '内容',
    `biz_type` TINYINT DEFAULT NULL COMMENT '业务类型：1商品 2订单 3卖家认证 4聊天 5举报',
    `biz_id` BIGINT DEFAULT NULL COMMENT '业务主键id',
    `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '读取状态：0未读 1已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '读取时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_notice_user_read` (`user_id`, `read_status`),
    KEY `idx_notice_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';


-- 12. 订单表
CREATE TABLE `order` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `seller_id` BIGINT NOT NULL COMMENT '卖家id，逻辑关联user.id',
    `buyer_id` BIGINT NOT NULL COMMENT '买家id，逻辑关联user.id',
    `goods_title` VARCHAR(100) NOT NULL COMMENT '商品标题',
    `goods_cover` VARCHAR(512) DEFAULT NULL COMMENT '商品封面',
    `goods_price` DECIMAL(10,2) NOT NULL COMMENT '下单价格',
    `seller_name` VARCHAR(50) DEFAULT NULL COMMENT '卖家名称',
    `seller_phone` VARCHAR(20) DEFAULT NULL COMMENT '卖家手机号',
    `buyer_name` VARCHAR(50) DEFAULT NULL COMMENT '买家名称',
    `buyer_phone` VARCHAR(20) DEFAULT NULL COMMENT '买家手机号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付 1已支付 2已完成 3已取消 4超时关闭',
    `expire_time` DATETIME NOT NULL COMMENT '支付过期时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `close_time` DATETIME DEFAULT NULL COMMENT '关闭时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `meet_location` VARCHAR(120) DEFAULT NULL COMMENT '面交地点',
    `meet_time` DATETIME DEFAULT NULL COMMENT '面交时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_buyer_status_time` (`buyer_id`, `status`, `create_time`),
    KEY `idx_order_seller_status_time` (`seller_id`, `status`, `create_time`),
    KEY `idx_order_goods` (`goods_id`),
    KEY `idx_order_status_expire` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';


-- 13. 支付记录表
CREATE TABLE `pay` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `pay_no` VARCHAR(32) NOT NULL COMMENT '支付单号',
    `request_no` VARCHAR(64) NOT NULL COMMENT '支付请求号，幂等控制',
    `order_id` BIGINT NOT NULL COMMENT '订单id，逻辑关联order.id',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '付款人id，逻辑关联user.id',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式：1模拟支付',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0待支付 1成功 2失败 3关闭',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_no` (`pay_no`),
    UNIQUE KEY `uk_pay_request_no` (`request_no`),
    UNIQUE KEY `uk_pay_order_id` (`order_id`),
    KEY `idx_pay_buyer_status` (`buyer_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';


-- 14. 聊天会话表
CREATE TABLE `chatsession` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `goods_id` BIGINT NOT NULL COMMENT '商品id，逻辑关联goods.id',
    `seller_id` BIGINT NOT NULL COMMENT '卖家id，逻辑关联user.id',
    `buyer_id` BIGINT NOT NULL COMMENT '买家id，逻辑关联user.id',
    `goods_title` VARCHAR(100) DEFAULT NULL COMMENT '商品标题',
    `goods_cover` VARCHAR(512) DEFAULT NULL COMMENT '商品封面',
    `last_msg` VARCHAR(255) DEFAULT NULL COMMENT '最后一条消息摘要',
    `last_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `seller_unread` INT NOT NULL DEFAULT 0 COMMENT '卖家未读数',
    `buyer_unread` INT NOT NULL DEFAULT 0 COMMENT '买家未读数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chatsession_goods_users` (`goods_id`, `seller_id`, `buyer_id`),
    KEY `idx_chatsession_seller_time` (`seller_id`, `last_time`),
    KEY `idx_chatsession_buyer_time` (`buyer_id`, `last_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';


-- 15. 聊天消息表
CREATE TABLE `chatmessage` (
    `id` BIGINT NOT NULL COMMENT '主键id',
    `session_id` BIGINT NOT NULL COMMENT '会话id，逻辑关联chatsession.id',
    `sender_id` BIGINT NOT NULL COMMENT '发送者id，逻辑关联user.id',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者id，逻辑关联user.id',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1文本 2图片 3系统',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态：0未读 1已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` BIGINT NOT NULL DEFAULT 0 COMMENT '修改人id',
    PRIMARY KEY (`id`),
    KEY `idx_chatmessage_session_time` (`session_id`, `create_time`),
    KEY `idx_chatmessage_receiver_read` (`receiver_id`, `read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
