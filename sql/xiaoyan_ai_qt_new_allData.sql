/*
 Navicat Premium Dump SQL

 Source Server         : v8.0.43-服务器
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : 119.29.228.36:21306
 Source Schema         : xiaoyan_ai_qt

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 17/03/2026 20:58:23
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_user
-- ----------------------------
INSERT INTO `admin_user` VALUES (1, 'admin', '$2a$10$7la0XsDBiVkCGVjNxmJzvOgVklJ6ltehmmHwZ6cCH4mUWGTAEadFG', 'ADMIN', 'admin@qq.com', '17838675012', '2026-03-12 15:40:17', '2026-02-03 23:53:42', '2026-02-04 21:16:24');

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
) ENGINE = InnoDB AUTO_INCREMENT = 5223 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of key_info
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_type
-- ----------------------------
INSERT INTO `product_type` VALUES (1, '一小时卡密', 'USES_1_HOUR', 60, 0, 0, '一小时无限次数，无限激活使用卡密', 1, 2, '2026-03-14 21:52:32', '2026-03-14 22:03:12');
INSERT INTO `product_type` VALUES (2, '10分钟体验卡密', 'USER_10_MINUTE', 10, 0, 0, '10分钟无限次数，无限激活使用卡密', 1, 1, '2026-03-14 22:02:49', '2026-03-14 22:03:10');
INSERT INTO `product_type` VALUES (3, '1天使用卡', 'USER_1_DAY', 1440, 0, 0, '1天无限次数，无限激活使用卡密', 1, 3, '2026-03-14 22:04:35', '2026-03-14 22:05:49');
INSERT INTO `product_type` VALUES (4, '30天使用卡', 'USER_30_DAY', 43200, 0, 0, '30天无限次数，无限激活使用卡密', 1, 4, '2026-03-14 22:05:34', '2026-03-14 22:05:51');

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
) ENGINE = InnoDB AUTO_INCREMENT = 230 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API请求日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of request_log
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局可变配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'dashscope.apiKey', 'sk-4de2b2e093d04d4fa9db1aae75261e46', 'dashscope', 'string', 1, '阿里云百炼模型部署请求密钥', '2025-12-26 19:14:23', '2025-12-26 19:14:23');
INSERT INTO `sys_config` VALUES (3, 'dashscope.model.v3.00', 'qwen2.5-7b-instruct-ft-202507191708-1bfd', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3主部署）现今效果最好', '2025-12-26 19:14:23', '2026-03-09 15:41:38');
INSERT INTO `sys_config` VALUES (4, 'dashscope.model.v3.09', 'qwen2.5-7b-instruct-acd444562558', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署9号）', '2026-03-09 15:36:22', '2026-03-09 15:48:39');
INSERT INTO `sys_config` VALUES (5, 'dashscope.model.v3.08', 'qwen2.5-7b-instruct-34480057836e', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署8号）', '2026-03-09 15:37:03', '2026-03-14 19:24:35');
INSERT INTO `sys_config` VALUES (6, 'dashscope.model.v3.07', 'qwen2.5-7b-instruct-3723d22db311', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署7号）', '2026-03-09 15:45:07', '2026-03-09 15:49:36');
INSERT INTO `sys_config` VALUES (7, 'dashscope.model.v3.06', 'qwen2.5-7b-instruct-4a669867cbe5', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署6号）', '2026-03-09 15:45:07', '2026-03-09 15:49:28');
INSERT INTO `sys_config` VALUES (8, 'dashscope.model.v3.05', 'qwen2.5-7b-instruct-9d2d70b76056', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署5号）', '2026-03-09 15:45:07', '2026-03-09 15:49:25');
INSERT INTO `sys_config` VALUES (9, 'dashscope.model.v3.04', 'qwen2.5-7b-instruct-3d343324b51d-123456', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署4号）', '2026-03-09 15:45:07', '2026-03-09 15:49:21');
INSERT INTO `sys_config` VALUES (10, 'dashscope.model.v3.03', 'qwen2.5-7b-instruct-9295c60556bb', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署3号）', '2026-03-09 15:45:07', '2026-03-09 15:49:16');
INSERT INTO `sys_config` VALUES (11, 'dashscope.model.v3.02', 'qwen2.5-7b-instruct-821d1c90ee3b-2', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署2号）', '2026-03-09 15:45:07', '2026-03-09 15:49:13');
INSERT INTO `sys_config` VALUES (12, 'dashscope.model.v3.01', 'qwen2.5-7b-instruct-893ae02b79c7-1', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署1号）', '2026-03-09 15:45:07', '2026-03-09 15:49:10');
INSERT INTO `sys_config` VALUES (13, 'system.version', 'v1.1.0', 'system', 'string', 1, '系统用户端软件版本', '2026-03-10 17:21:46', '2026-03-17 16:54:57');
INSERT INTO `sys_config` VALUES (14, 'system.updateLink', 'https://link3.cc/yanchenai', 'system', 'int', 1, '', '2026-03-11 17:39:33', '2026-03-16 15:42:07');
INSERT INTO `sys_config` VALUES (17, 'apikey.usageType.ldxp', '链动小铺售卖', 'apikey', 'string', 1, '卡密用途，不要改动其config_key', '2026-03-15 22:02:26', '2026-03-15 22:10:42');
INSERT INTO `sys_config` VALUES (20, 'apikey.usageType.xianyu', '闲鱼售卖', 'apikey', 'string', 1, '卡密用途，不要改动其config_key', '2026-03-15 22:03:20', '2026-03-15 22:10:42');
INSERT INTO `sys_config` VALUES (22, 'apikey.usageType.free', '免费', 'apikey', 'string', 1, '卡密用途，不要改动其config_key', '2026-03-15 22:03:42', '2026-03-15 22:10:43');

SET FOREIGN_KEY_CHECKS = 1;
