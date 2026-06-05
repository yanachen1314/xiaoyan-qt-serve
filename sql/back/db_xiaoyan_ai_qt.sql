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

 Date: 13/03/2026 17:32:56
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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_user
-- ----------------------------
INSERT INTO `admin_user` VALUES (1, 'admin', '$2a$10$7la0XsDBiVkCGVjNxmJzvOgVklJ6ltehmmHwZ6cCH4mUWGTAEadFG', 'ADMIN', 'admin@qq.com', '17838675012', '2026-03-10 15:06:07', '2026-02-03 23:53:42', '2026-02-04 21:16:24');

-- ----------------------------
-- Table structure for key_info
-- ----------------------------
DROP TABLE IF EXISTS `key_info`;
CREATE TABLE `key_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_value` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '32位密钥值，唯一索引',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '密钥状态: 0-禁用, 1-启用',
  `total_uses` int NOT NULL DEFAULT 0 COMMENT '总使用次数(降AI)，0表示无限制',
  `remaining_uses` int NOT NULL DEFAULT 0 COMMENT '剩余使用次数(降AI)，表示已用完或无限制。total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses必须设置0表示无限制',
  `product_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品类型或套餐标识，例如: \"ai_rewrite_vip\", \"ai_rewrite_basic\"',
  `activations_num` int NOT NULL DEFAULT 0 COMMENT '激活次数，0-无次数限制',
  `remaining_active_num` int NOT NULL DEFAULT 0 COMMENT '剩余激活次数，表示已用完或无限制。activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum必须设置0表示无限制',
  `activated` tinyint NOT NULL DEFAULT 0 COMMENT '激活状态: 0-未激活, 1-已激活',
  `activation_device` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '激活设备',
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
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of key_info
-- ----------------------------
INSERT INTO `key_info` VALUES (1, 'OVIEG66E-AGGBSPVS-J1BFHCT1-7XKG12DF', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '12', '2026-03-11 00:13:00', '2026-03-11 01:13:00', '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-11 15:08:09');
INSERT INTO `key_info` VALUES (2, 'ZD1JYFFG-9BSOW6PK-BYLULAVB-XDEXRYX1', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '12', '2026-03-11 18:07:24', '2026-03-11 19:07:24', '激活时间: 2026-03-11 18:07:23 | 设备: 12', 1, '2026-03-10 17:17:00', '2026-03-11 18:07:23');
INSERT INTO `key_info` VALUES (3, 'IVOPFX48-S8DYGSSC-MST5SPA8-SF8MGNQX', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (4, '5QUWCKEE-WGCIBZU6-QF7HGB7X-0QH790VE', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (5, 'C4DWPP8Q-N9IJZOXI-6XAP9O9X-SC60APS4', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (6, 'U9ZQNHXO-FMZN2ZMS-F75RAZYM-MKCAYCIM', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (7, 'BT716D3T-1X7YFRUN-Y8BI1V1F-ZUCD50T6', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (8, '70189UYF-0AVQVRIL-BLP69TBZ-D5G07OED', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', '2026-03-11 20:17:15', '2026-03-11 21:17:15', '激活时间: 2026-03-11 20:17:14 | 设备: 9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', 1, '2026-03-10 17:17:00', '2026-03-11 20:17:14');
INSERT INTO `key_info` VALUES (9, '5MAHQG6L-R2NCMQRA-RI1EVP2I-ASVWPPRU', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (10, 'UACPTHOH-M1W23KT3-7MR4Q2KR-6VLDBF05', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (11, 'EZZJ2H3B-R42SGWT3-AHWBICJP-85BINQ4P', 1, 0, 0, 'ai_rewrite_basic', 1, -1, 1, '9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', '2026-03-11 20:58:23', '2026-03-11 21:00:00', '激活时间: 2026-03-11 20:58:23 | 设备: 9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', 1, '2026-03-10 17:17:00', '2026-03-11 21:03:24');
INSERT INTO `key_info` VALUES (12, '7U6J988A-NVFVKATH-BDDXUDR8-48H1SGNS', 1, 0, 0, 'ai_rewrite_basic', 1, -1, 1, '9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', '2026-03-11 20:58:48', '2026-03-11 21:00:00', '激活时间: 2026-03-11 20:58:47 | 设备: 9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', 1, '2026-03-10 17:17:00', '2026-03-11 21:03:36');
INSERT INTO `key_info` VALUES (13, 'P6N6XD46-AAR04GEU-021KNTV6-QWT1KJMR', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '6b30b89c-df0e-52f4-89f6-e9bfd365a195', '2026-03-11 20:03:04', '2026-03-11 21:00:00', '激活时间: 2026-03-11 20:03:04 | 设备: 6b30b89c-df0e-52f4-89f6-e9bfd365a195', 1, '2026-03-10 17:17:00', '2026-03-11 21:03:54');
INSERT INTO `key_info` VALUES (14, '45714BXI-TBXW71FC-BOS9YZQG-6ZOICFP3', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '6b30b89c-df0e-52f4-89f6-e9bfd365a195', '2026-03-11 20:26:32', '2026-03-11 21:00:00', '激活时间: 2026-03-11 20:26:31 | 设备: 6b30b89c-df0e-52f4-89f6-e9bfd365a195', 1, '2026-03-10 17:17:00', '2026-03-11 21:03:57');
INSERT INTO `key_info` VALUES (15, 'UZQ00ZON-HP0ZOS3R-7TGZ0NTD-BV514FS8', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '6b30b89c-df0e-52f4-89f6-e9bfd365a195', '2026-03-11 20:54:18', '2026-03-11 21:00:00', '激活时间: 2026-03-11 20:54:18 | 设备: 6b30b89c-df0e-52f4-89f6-e9bfd365a195', 1, '2026-03-10 17:17:00', '2026-03-11 21:04:01');
INSERT INTO `key_info` VALUES (16, 'PYYX6T5G-LQWT5CT1-MO29G5VX-0FNSVQI5', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '6b30b89c-df0e-52f4-89f6-e9bfd365a195', '2026-03-11 21:08:09', '2026-03-11 22:08:09', '激活时间: 2026-03-11 21:08:09 | 设备: 6b30b89c-df0e-52f4-89f6-e9bfd365a195', 1, '2026-03-10 17:17:00', '2026-03-11 21:08:09');
INSERT INTO `key_info` VALUES (17, 'UFUVJABF-I245VEO0-00R5GO3O-I8ZWFIEB', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (18, 'TEAF87DF-84GI4YP4-GOPOEJ6B-JZ5V91DA', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (19, 'QGWWD077-AQUR0K6R-4M8IAL0E-JOG2B02E', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (20, 'PZMJUXIT-GDXY87WW-6AYAVBMU-45E7AE0G', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (21, 'KJTC3CF0-DN9P62EE-Z7HXEDS9-I70Q29JG', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (22, 'KDM7U6Q1-LSM51LY0-EB29U33S-QAU9WPED', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (23, 'VYDW88N1-19IEAIX4-D4QNTX8C-I84XDHJH', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (24, 'SRIMOF5A-SZD5NJX9-SV69TCYM-YF2GHG2G', 1, 0, 0, 'ai_rewrite_basic', 1, 0, 1, '9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', '2026-03-11 20:08:13', '2026-03-11 21:08:13', '激活时间: 2026-03-11 20:08:13 | 设备: 9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', 1, '2026-03-10 17:17:00', '2026-03-11 20:08:13');
INSERT INTO `key_info` VALUES (25, '0EQ10QQV-ND1208PM-SCLKA82K-6XOPO1ZJ', 1, 0, 0, 'ai_rewrite_basic', 1, -5, 1, '9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', '2026-03-11 20:52:52', '2026-03-11 21:52:52', '激活时间: 2026-03-11 20:52:51 | 设备: 9d6376e8-7c2b-537e-bf36-ceb0bbffda5f', 1, '2026-03-10 17:17:00', '2026-03-11 20:52:51');
INSERT INTO `key_info` VALUES (26, 'JNRLFN3M-4GRQTD80-XT9NEDVI-ZNYN3RBZ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (27, 'SH5SS17T-3OJCARIH-763T1YFM-AW0L731A', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (28, 'B5TDK8C2-A1IV1KG7-1DN5KYY8-82DJ73SO', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (29, 'GL07P5BK-Q8TUJAGJ-AT5PYRW2-PPHE9O7K', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (30, 'IQYS1V6G-S13CKKEM-SAEXJY97-IK1GTPTQ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (31, 'JDD6CK0X-VA5391CT-UCB3CT46-59WX12PC', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (32, '5GJWVO8J-84G4NWPM-M52FN9QL-MZ4KUGVM', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (33, 'PC4KEICT-Y8767DYI-CGU77ZKA-JK0544SV', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (34, '3CBPE5CP-KS15FOGF-VWAQL32S-MYFVJXXA', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (35, 'TPTH41LJ-SFCBH5U5-34XXQEFF-ZJ0U3G8Y', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (36, 'V5NY0RBS-S7743ZP9-XKQN2FTL-MVVGX00A', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (37, 'JY6EGOZ6-ZI2AG2TV-0JEDYZWW-6SI1ADD6', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (38, '8IUNOVT8-J7Q6M2NL-G7NW9QA6-E9UFHMEO', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (39, 'STYS8X7R-R64YMCSZ-OOZDJRKU-VNWOCMH8', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (40, 'K9RT50TN-LX5UMIAX-V8NAF2EB-K1TZJ147', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (41, '9YLSJWGH-ACJ7DMHR-65JNC30R-5OX543F5', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (42, '1D4T3W30-G2INFVPE-RDQJEYYK-9JZNVVCN', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (43, 'GM6Y4PBO-Q11WY7WZ-HC4M32MV-BN389JYH', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (44, 'URPS2Y8R-2AHWP55P-AWLM12YY-G5XCN920', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (45, '6P4NBP5A-FRF4AX4K-60KP38C0-VXR5SQY6', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (46, 'WSYSB002-P39FCFPN-35J72UWC-VE7BI92Q', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (47, 'TVJD3Y4A-WTAJ5Z2C-FSAM5FMQ-LRAERC7F', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (48, 'JOCNMAOY-LZA74JXW-0I0CV4FR-HAFQQ5IM', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (49, 'MBK8IFC2-NB43M4QZ-T4PDVU4E-4LLXBE4J', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (50, 'BVUK0VJY-DKU81WJU-FZEDG64Z-7CXU5GJE', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (51, 'R9H59B3W-5MBIFSHA-7JOJ0WN4-BPO1RU0C', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (52, 'C5S1PIKO-1EFHVNI0-4STA0LGR-4XOKVMT9', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (53, 'P2YDHUM0-WZ0NLP0P-BN1DRVJJ-0XAOG6AY', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (54, '0NW3AP1Z-LGZWE5RM-VWN1SXUD-Y6BYYK2I', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (55, '3Z0XFTOO-ODSSL1M5-U65TQFMH-MA2PTARD', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (56, '2Y7YVQT5-RRIK5J0O-UWZF58JM-I8D1FHNH', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (57, '1Y3IBP61-1ABHL2B1-DX2UD2MQ-YEU14XAQ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (58, 'BOFKHBET-ZXWIFY6O-VRZGQ38M-9PDB5HEV', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (59, '29UFOPBA-SUJ7AFUS-8U9O58IY-B0QQ4TGQ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (60, 'ROH9VXMI-3C3EUEV6-IW98U8K2-JOYBX0DX', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (61, 'AM2WOW40-NHUSWLDN-GJ3TNALR-YZDH3KR2', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (62, 'UGIS2M1C-6J1G5U46-TTAEA0SA-HO00VW5J', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (63, '7E6UN9KE-VWCJSV75-S2O6JRHX-EV7JTP5E', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (64, 'DS2BBN93-W0ZODB64-0NHNOZHQ-C102GQ1I', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (65, 'R7V5254J-8S7XOFZI-T30UVVDO-1ZVUAC1N', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (66, 'TP6F4JO5-YJ378L0H-DS4JTZ05-Q9K0D4FA', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (67, 'ONYXBJJU-HMV8LIXY-UTWGH6XP-DQSWVQJF', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (68, 'O49B21GD-TKQF011B-2VI1OQXK-JNPV2Y61', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (69, 'W53D3L6J-PTRRJQGS-P8LGE8LD-GWZT1G5O', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (70, 'Y9S7VVDX-HWZD4HKW-77R5Y9N7-9PGIR13P', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (71, '6VQ3P776-FR6YN5X3-FJGYQHPN-J3JI99TO', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (72, 'W1X2WBHI-DFD9GLLN-KHX3QFKO-UI09W3JZ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (73, 'TCLJ5XBR-3N39LYM9-3XNW2Z9U-V4MOA6CD', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (74, '4NFVHCG7-CLKKGH49-19K34PM3-DCOTZUXX', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (75, 'Z1X11E0A-NDJVFYL2-RT14EEHZ-4SSQHO16', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (76, '9CYQMMXL-JY6MJ56I-8AXV42S0-4NKRKZXY', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (77, 'K8XF8DZK-5WRYACAZ-8B1M7HTB-JOY5MN6G', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (78, 'A0UWMBHW-YCBX7WC8-F17R2J78-MLIJYZWM', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (79, 'E96NPOZK-7A4JDF78-0NBDT3GO-36EE5T2D', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (80, '5GI7VHR0-V43FH9D7-Q4FTA3YX-5JM9U5ZL', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (81, '1YI91C8A-V14KJ2M0-VXVV02FK-F569P7V0', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (82, 'ZO9AZIXI-690HX1KQ-YGFM4J6O-TOHS6MRC', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (83, '9CWT0J7K-STRYEPD7-E6GSN3AR-XSZEPVF8', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (84, 'NI74XOZE-I29LGHP5-AFINQQ1K-INAMVADY', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (85, 'QO385PHS-ZF7SOV58-N073E53F-BIYRVW2G', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (86, 'QOY66FDI-MX2TSX1W-J3GINZN1-SR1I3OS9', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (87, 'P6CAVERL-4I4KDVBV-RGXBKSI0-4A3D3N3K', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (88, 'HUOETAI1-1B4MK1ML-HRRSLH7O-G6ECSCQ3', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (89, 'IHIXU5WB-YF1WZP3H-58VAN028-PIWY105J', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (90, 'RMW12HUK-X02TTK3U-XUI2FHTO-VG4LA7DZ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (91, '0DAAIOCA-0KXW4SEF-NR2GWA32-VFPI0DCK', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (92, '2LRJ7NNH-OD2DMUW2-AF7TA87V-CEGFVXXU', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (93, 'T0GU57E5-3M6TGJDH-KER8WVUT-A18MFB2U', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (94, '59BKOSR1-6I0MZFAR-PSXIIPU9-3NK1BM9F', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (95, 'MM5H3OGQ-HU19C2CQ-OA2NG92Y-MENDJ1YI', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (96, 'QQ40ISLM-7YRO45JX-CHLKFSY0-K9UZWMD6', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (97, 'MMN505LE-WJFF4L03-YHDST4EU-TJM4Q6FP', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (98, 'I11WI6RP-JYWT0PGW-P54S26DI-GIDK68A1', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (99, '2ED22P3B-IWAVP8YI-BVTT3BZ1-S4JJE060', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');
INSERT INTO `key_info` VALUES (100, '9SV1V60D-W3FYZUB3-F0AVKI91-BJZ3ULPZ', 1, 0, 0, 'ai_rewrite_basic', 1, 1, 0, NULL, NULL, NULL, '一小时无限制使用的密钥', 1, '2026-03-10 17:17:00', '2026-03-10 18:19:41');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'API请求日志表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局可变配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'dashscope.apiKey', 'sk-4de2b2e093d04d4fa9db1aae75261e46', 'dashscope', 'string', 1, '阿里云百炼模型部署请求密钥', '2025-12-26 19:14:23', '2025-12-26 19:14:23');
INSERT INTO `sys_config` VALUES (3, 'dashscope.model.v3.00', 'qwen2.5-7b-instruct-ft-202507191708-1bfd', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3主部署）现今效果最好', '2025-12-26 19:14:23', '2026-03-09 15:41:38');
INSERT INTO `sys_config` VALUES (4, 'dashscope.model.v3.09', 'qwen2.5-7b-instruct-acd444562558', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署9号）', '2026-03-09 15:36:22', '2026-03-09 15:48:39');
INSERT INTO `sys_config` VALUES (5, 'dashscope.model.v3.08', 'qwen2.5-7b-instruct-34480057836e', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署8号）', '2026-03-09 15:37:03', '2026-03-09 15:48:44');
INSERT INTO `sys_config` VALUES (6, 'dashscope.model.v3.07', 'qwen2.5-7b-instruct-3723d22db311', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署7号）', '2026-03-09 15:45:07', '2026-03-09 15:49:36');
INSERT INTO `sys_config` VALUES (7, 'dashscope.model.v3.06', 'qwen2.5-7b-instruct-4a669867cbe5', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署6号）', '2026-03-09 15:45:07', '2026-03-09 15:49:28');
INSERT INTO `sys_config` VALUES (8, 'dashscope.model.v3.05', 'qwen2.5-7b-instruct-9d2d70b76056', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署5号）', '2026-03-09 15:45:07', '2026-03-09 15:49:25');
INSERT INTO `sys_config` VALUES (9, 'dashscope.model.v3.04', 'qwen2.5-7b-instruct-3d343324b51d-123456', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署4号）', '2026-03-09 15:45:07', '2026-03-09 15:49:21');
INSERT INTO `sys_config` VALUES (10, 'dashscope.model.v3.03', 'qwen2.5-7b-instruct-9295c60556bb', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署3号）', '2026-03-09 15:45:07', '2026-03-09 15:49:16');
INSERT INTO `sys_config` VALUES (11, 'dashscope.model.v3.02', 'qwen2.5-7b-instruct-821d1c90ee3b-2', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署2号）', '2026-03-09 15:45:07', '2026-03-09 15:49:13');
INSERT INTO `sys_config` VALUES (12, 'dashscope.model.v3.01', 'qwen2.5-7b-instruct-893ae02b79c7-1', 'dashscope', 'string', 1, '阿里云百炼模型部署名称（code，v3分部署1号）', '2026-03-09 15:45:07', '2026-03-09 15:49:10');
INSERT INTO `sys_config` VALUES (13, 'system.version', 'v1.0.0', 'system', 'string', 1, '系统服务版本', '2026-03-10 17:21:46', '2026-03-11 23:26:13');
INSERT INTO `sys_config` VALUES (14, 'system.updateLink', 'http://xygo.cn', 'system', 'string', 1, NULL, '2026-03-11 17:39:33', '2026-03-11 20:47:33');

SET FOREIGN_KEY_CHECKS = 1;
