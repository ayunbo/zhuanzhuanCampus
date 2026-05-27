SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `searchhistory`;
CREATE TABLE `searchhistory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL COMMENT '用户id，逻辑关联user.id',
  `query_sign` varchar(64) NOT NULL COMMENT '搜索条件签名，用于去重',
  `keyword` varchar(100) DEFAULT NULL COMMENT '搜索关键词',
  `category_id` bigint DEFAULT NULL COMMENT '分类id',
  `seller_id` bigint DEFAULT NULL COMMENT '卖家id',
  `status` tinyint DEFAULT NULL COMMENT '商品状态',
  `quality` tinyint DEFAULT NULL COMMENT '成色',
  `location` varchar(120) DEFAULT NULL COMMENT '位置或校区',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '最低价格',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '最高价格',
  `sort_by` varchar(20) DEFAULT NULL COMMENT '排序方式',
  `search_count` int NOT NULL DEFAULT 1 COMMENT '搜索次数',
  `last_search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `update_user` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_searchhistory_user_sign` (`user_id`, `query_sign`),
  KEY `idx_searchhistory_user_time` (`user_id`, `last_search_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户搜索历史表';

SET FOREIGN_KEY_CHECKS = 1;
