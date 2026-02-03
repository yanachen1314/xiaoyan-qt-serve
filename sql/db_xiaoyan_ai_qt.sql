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

 Date: 03/02/2026 22:54:53
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
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色，例如: ADMIN, OPERATOR',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱地址，可选',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话，可选',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for key_info
-- ----------------------------
DROP TABLE IF EXISTS `key_info`;
CREATE TABLE `key_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_value` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '32位密钥值，唯一索引',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '密钥状态: 0-禁用, 1-启用',
  `total_uses` int NOT NULL DEFAULT 0 COMMENT '总使用次数，0表示无限制',
  `remaining_uses` int NOT NULL DEFAULT 0 COMMENT '剩余使用次数，0表示无限制或已用完',
  `product_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品类型或套餐标识，例如: \"ai_rewrite_vip\", \"ai_rewrite_basic\"',
  `activated_time` datetime NULL DEFAULT NULL COMMENT '激活时间',
  `expiry_time` datetime NULL DEFAULT NULL COMMENT '过期时间，可为空表示永不过期',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `admin_created_by` bigint NULL DEFAULT NULL COMMENT '创建该密钥的管理员ID，关联admin_user表',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `key_value`(`key_value` ASC) USING BTREE,
  INDEX `idx_key_value`(`key_value` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_remaining_uses`(`remaining_uses` ASC) USING BTREE,
  INDEX `idx_expiry_time`(`expiry_time` ASC) USING BTREE,
  INDEX `idx_admin_created_by`(`admin_created_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for request_log
-- ----------------------------
DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_value_used` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '本次请求使用的密钥',
  `client_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发起请求的客户端IP地址 (支持IPv4/v6)',
  `request_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_code` int NOT NULL COMMENT 'HTTP响应状态码，例如 200, 402, 401',
  `processing_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果摘要或错误信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_key_value_used`(`key_value_used` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  INDEX `idx_client_ip`(`client_ip` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API请求日志表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
