-- =============================================
-- 多邮箱轮询发送功能 - 数据库表结构
-- 创建时间: 2026-04-22
-- 说明: 用于支持多SMTP账号管理和发送统计
-- =============================================

-- ----------------------------
-- 邮件账号配置表
-- ----------------------------
DROP TABLE IF EXISTS `mail_account`;
CREATE TABLE `mail_account`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email_address`        varchar(100) NOT NULL COMMENT '邮箱地址',
    `host`                 varchar(100) NOT NULL COMMENT 'SMTP服务器地址',
    `port`                 int          NOT NULL COMMENT 'SMTP端口',
    `password`             varchar(200) NOT NULL COMMENT '邮箱密码/授权码',
    `password_create_time` datetime     DEFAULT NULL COMMENT '密码或授权码创建时间',
    `protocol`             varchar(20)  DEFAULT 'smtp' COMMENT '协议类型',
    `ssl_enable`           tinyint      DEFAULT 1 COMMENT '是否启用SSL: 0-否, 1-是',
    `status`               tinyint      DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `daily_limit`          int          DEFAULT 500 COMMENT '每日发送上限',
    `priority`             int          DEFAULT 0 COMMENT '优先级，数值越小优先级越高',
    `remark`               varchar(255) DEFAULT NULL COMMENT '备注',
    `created_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_email_address` (`email_address`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='邮件账号配置表';

-- ----------------------------
-- 邮件发送统计表
-- ----------------------------
DROP TABLE IF EXISTS `mail_send_record`;
CREATE TABLE `mail_send_record`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email_address` varchar(100) NOT NULL COMMENT '邮箱地址',
    `send_date`     date         NOT NULL COMMENT '发送日期',
    `send_count`    int      DEFAULT 0 COMMENT '发送数量',
    `success_count` int      DEFAULT 0 COMMENT '成功数量',
    `fail_count`    int      DEFAULT 0 COMMENT '失败数量',
    `created_time`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_email_date` (`email_address`, `send_date`) USING BTREE,
    KEY `idx_send_date` (`send_date`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='邮件发送统计表';


-- ----------------------------
-- 免费领取卡密记录表
-- ----------------------------
DROP TABLE IF EXISTS `free_license_record`;
CREATE TABLE `free_license_record`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email`        varchar(100) NOT NULL COMMENT '领取邮箱',
    `license_key`  varchar(100) NOT NULL COMMENT '领取的卡密',
    `device_id`    varchar(255) DEFAULT NULL COMMENT '设备ID/设备指纹',
    `client_ip`    varchar(50)  DEFAULT NULL COMMENT '领取时的客户端IP地址（支持IPv4/IPv6）',
    `user_agent`   varchar(500) DEFAULT NULL COMMENT '用户代理信息（浏览器/客户端标识）',
    `created_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_email` (`email`) USING BTREE COMMENT '一个邮箱只能领取一次',
    KEY `idx_created_time` (`created_time`) USING BTREE,
    KEY `idx_device_id` (`device_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='免费领取卡密记录表';


-- ----------------------------
-- 初始化示例数据（可选，根据实际情况添加）
-- ----------------------------
INSERT INTO `mail_account` (`email_address`, `host`, `port`, `password`, `password_create_time`, `protocol`,
                            `ssl_enable`, `status`, `daily_limit`, `priority`, `remark`)
VALUES ('284257280@qq.com', 'smtp.qq.com', 465, 'ldnkohaquidwcbah', NOW(), 'smtp', 1, 1, 200, 0, 'QQ邮箱主账号'),
       ('3923763688@qq.com', 'smtp.qq.com', 465, 'oyxlrauwcikjcfgc', NOW(), 'smtp', 1, 1, 200, 1, 'QQ邮箱备用账号'),
       ('xiaoyanai888@163.com', 'smtp.163.com', 465, 'BNrwt6dAuBCPa7iN', NOW(), 'smtp', 1, 1, 200, 2,
        '163邮箱备用账号');
