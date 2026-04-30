/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : db_xiaoyan_ai_qt

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 30/04/2026 14:49:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员账号名，用于登录',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希值，推荐使用BCrypt等强哈希算法',
  `role` enum('ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色，例如: ADMIN, OPERATOR',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱地址，可选',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话，可选',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for key_info
-- ----------------------------
DROP TABLE IF EXISTS `key_info`;
CREATE TABLE `key_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_value` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '32位密钥值，唯一索引',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '密钥状态: 0-禁用, 1-启用',
  `selled` tinyint NOT NULL DEFAULT 0 COMMENT '密钥销售状态（主要针对非链动小铺场景）：0-未销售，1-已销售',
  `usage_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密钥用途，详见配置',
  `total_uses` int NOT NULL DEFAULT 0 COMMENT '总使用次数(降AI)，0表示无限制',
  `remaining_uses` int NOT NULL DEFAULT 0 COMMENT '剩余使用次数(降AI)，表示已用完或无限制。total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses必须设置0表示无限制',
  `product_type` int NOT NULL COMMENT '对应产品ID',
  `activations_num` int NOT NULL DEFAULT 0 COMMENT '激活次数，0-无次数限制',
  `remaining_active_num` int NOT NULL DEFAULT 0 COMMENT '剩余激活次数，表示已用完或无限制。activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum必须设置0表示无限制',
  `activated` tinyint NOT NULL DEFAULT 0 COMMENT '激活状态: 0-未激活, 1-已激活',
  `activation_device` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '激活设备',
  `activation_time` datetime NULL DEFAULT NULL COMMENT '激活时间',
  `expiry_time` datetime NULL DEFAULT NULL COMMENT '过期时间，可为空表示永不过期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建该密钥的管理员ID，关联admin_user表',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `key_value`(`key_value` ASC) USING BTREE,
  INDEX `idx_key_value`(`key_value` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_remaining_uses`(`remaining_uses` ASC) USING BTREE,
  INDEX `idx_expiry_time`(`expiry_time` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5641 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mail_account
-- ----------------------------
DROP TABLE IF EXISTS `mail_account`;
CREATE TABLE `mail_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  `host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SMTP服务器地址',
  `port` int NOT NULL COMMENT 'SMTP端口',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱密码/授权码',
  `password_create_time` datetime NULL DEFAULT NULL COMMENT '密码或授权码创建时间',
  `protocol` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'smtp' COMMENT '协议类型',
  `ssl_enable` tinyint NULL DEFAULT 1 COMMENT '是否启用SSL: 0-否, 1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `daily_limit` int NULL DEFAULT 500 COMMENT '每日发送上限',
  `priority` int NULL DEFAULT 0 COMMENT '优先级，数值越小优先级越高',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email_address`(`email_address` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件账号配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mail_free_key_record
-- ----------------------------
DROP TABLE IF EXISTS `mail_free_key_record`;
CREATE TABLE `mail_free_key_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '领取邮箱',
  `verify_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱验证token',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未验证, 1-已验证, 2-已发送卡密',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '验证链接过期时间',
  `license_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '领取的卡密（验证通过后分配）',
  `device_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型（web：网页端，app：win APP端）',
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备ID/设备指纹',
  `client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '领取时的客户端IP地址（支持IPv4/IPv6）',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理信息（浏览器/客户端标识）',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE COMMENT '一个邮箱只能领取一次',
  UNIQUE INDEX `uk_verify_token`(`verify_token` ASC) USING BTREE COMMENT '验证token唯一索引',
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '免费领取卡密记录表（邮箱验证版）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mail_send_record
-- ----------------------------
DROP TABLE IF EXISTS `mail_send_record`;
CREATE TABLE `mail_send_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_sender_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统发送者邮箱',
  `applicant_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请者邮箱',
  `send_date` date NOT NULL COMMENT '发送日期',
  `result_status` int NULL DEFAULT 0 COMMENT '结果状态，0：失败，1：成功',
  `result_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '结果信息',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_send_date`(`send_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件发送统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product_type
-- ----------------------------
DROP TABLE IF EXISTS `product_type`;
CREATE TABLE `product_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
  `product_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品编码',
  `equity_time` int NOT NULL DEFAULT 0 COMMENT '产品所用权益价值，单位：分钟',
  `equity_usage_num` int NOT NULL DEFAULT 0 COMMENT '产品所代表的使用次数（降AI），0：无限制，>0：有限使用次数',
  `equity_active_num` int NOT NULL DEFAULT 0 COMMENT '产品所代表的卡密激活次数，0：无限制，>0：有限激活次数',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '产品状态: 0-禁用, 1-启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_name`(`product_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for request_log
-- ----------------------------
DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求UUID',
  `requester` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求者',
  `client_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求者的IP地址 (支持IPv4/v6)',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求接口',
  `request_type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求类别，如：公开请求、管理员请求、卡密请求',
  `request_time` datetime NOT NULL COMMENT '请求开始时间',
  `request_duration` bigint NOT NULL COMMENT '请求耗时（毫秒ms）',
  `http_code` int NOT NULL COMMENT 'HTTP响应状态码，例如：200, 402, 401',
  `biz_code` int NULL DEFAULT NULL COMMENT '业务状态码，例如：200, 402, 5101',
  `response_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果摘要或错误信息',
  `input_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '降AI输入文本',
  `output_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '降AI输出文本',
  `input_text_chars` int NULL DEFAULT 0 COMMENT '降AI输入文本的字符数',
  `input_tokens` int NULL DEFAULT 0 COMMENT '降AI输入文本的token数',
  `output_tokens` int NULL DEFAULT 0 COMMENT '降AI输出文本的token数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '本次降AI的总token数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_id`(`request_id` ASC) USING BTREE,
  INDEX `idx_requestor`(`requester` ASC) USING BTREE,
  INDEX `idx_request_type`(`request_type` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  INDEX `idx_client_ip`(`client_ip` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1700 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API请求日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项 Key，如 system.email、dashscope.apiKey',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
  `config_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组，如 system、dashscope',
  `config_type` enum('string','int','boolean','json') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'string' COMMENT '类型：string、int、boolean、json',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态，1：启用，0：禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明/备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局可变配置表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
