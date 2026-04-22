-- =============================================
-- 多邮箱轮询发送功能 - 数据库表结构
-- 创建时间: 2026-04-22
-- 说明: 用于支持多SMTP账号管理和发送统计
-- =============================================

-- ----------------------------
-- 邮件账号配置表
-- ----------------------------
DROP TABLE IF EXISTS `mail_account`;
CREATE TABLE `mail_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  `host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SMTP服务器地址',
  `port` int NOT NULL COMMENT 'SMTP端口',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱密码/授权码',
  `protocol` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'smtp' COMMENT '协议类型',
  `ssl_enable` tinyint DEFAULT 1 COMMENT '是否启用SSL: 0-否, 1-是',
  `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `daily_limit` int DEFAULT 500 COMMENT '每日发送上限',
  `priority` int DEFAULT 0 COMMENT '优先级，数值越小优先级越高',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_email_address` (`email_address`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮件账号配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 邮件发送统计表
-- ----------------------------
DROP TABLE IF EXISTS `mail_send_record`;
CREATE TABLE `mail_send_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  `send_date` date NOT NULL COMMENT '发送日期',
  `send_count` int DEFAULT 0 COMMENT '发送数量',
  `success_count` int DEFAULT 0 COMMENT '成功数量',
  `fail_count` int DEFAULT 0 COMMENT '失败数量',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_email_date` (`email_address`, `send_date`) USING BTREE,
  KEY `idx_send_date` (`send_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮件发送统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 初始化示例数据（可选，根据实际情况添加）
-- ----------------------------
-- INSERT INTO `mail_account` (`email_address`, `host`, `port`, `password`, `protocol`, `ssl_enable`, `status`, `daily_limit`, `priority`, `remark`) 
-- VALUES 
-- ('2742015729@qq.com', 'smtp.qq.com', 465, 'your_qq_authorization_code', 'smtp', 1, 1, 500, 0, 'QQ邮箱主账号'),
-- ('xiaoyanai888@163.com', 'smtp.163.com', 465, 'your_163_authorization_code', 'smtp', 1, 1, 500, 1, '163邮箱备用账号');
