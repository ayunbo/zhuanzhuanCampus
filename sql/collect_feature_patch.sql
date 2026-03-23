-- 收藏功能补丁（如果主库脚本已包含 favorite 表，可忽略）

CREATE TABLE IF NOT EXISTS `favorite` (
    `id` bigint NOT NULL COMMENT '主键id',
    `user_id` bigint NOT NULL COMMENT '用户id',
    `goods_id` bigint NOT NULL COMMENT '商品id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人id',
    `update_user` bigint NOT NULL DEFAULT 0 COMMENT '更新人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`),
    KEY `idx_favorite_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
