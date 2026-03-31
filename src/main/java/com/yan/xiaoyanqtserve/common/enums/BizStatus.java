package com.yan.xiaoyanqtserve.common.enums;

import org.springframework.lang.Nullable;

/**
 * 业务状态码
 * 包含通用的HTTP状态码以及自定义的业务状态码
 * @author yanchen
 */
public enum BizStatus {
    // =========== HTTP 状态码 ==========
    //请求成功
    SUCCESS(200, "请求成功"),
    //对象创建成功
    CREATED(201, "对象创建成功"),
    //请求已经被接受
    ACCEPTED(202, "请求已经被接受"),
    // 操作已经执行成功，但是没有返回数据
    NO_CONTENT(204, "操作已经执行成功，但是没有返回数据"),

    //资源已被移除
    MOVED_PERM(301, "资源已被移除"),
    //重定向
    SEE_OTHER(303, "重定向"),
    //资源没有被修改
    NOT_MODIFIED(304, "资源没有被修改"),

    //参数列表错误（缺少，格式不匹配）
    BAD_REQUEST(400, "参数列表错误（缺少，格式不匹配）"),
    //未授权
    UNAUTHORIZED(401, "未授权"),
    //访问受限，授权过期
    FORBIDDEN(403, "权限不足，禁止访问"),
    //资源，服务未找到
    NOT_FOUND(404, "资源，服务未找到"),
    //不允许的http方法
    BAD_METHOD(405, "不允许的http方法"),
    // 资源冲突，或者资源被锁
    CONFLICT(409, "资源冲突，或者资源被锁"),
    //不支持的数据，媒体类型
    UNSUPPORTED_TYPE(415, "不支持的数据，媒体类型"),

    //系统内部错误
    ERROR(500, "系统内部错误"),

    // ======== 自定义业务状态码 ==========
    //接口未实现
    NOT_IMPLEMENTED(1101, "接口未实现"),
    // 验证码已发送
    CODE_HAS_SENT(1201, "验证码已发送，请勿重复发送！"),

    KEY_VALID(1500, "密钥验证成功"),
    KEY_INVALID_STATE(1501, "密钥数值非法"),
    KEY_INVALID_FORMAT(1502, "密钥格式非法"),
    KEY_NOT_FOUND(1503, "密钥不存在"),
    KEY_INACTIVE(1504, "密钥未启用"),
    KEY_EXPIRED(1505, "密钥已过期"),

    KEY_NOT_ACTIVATED(1506, "密钥未激活"),
    KEY_NON_ACTIVATIONS(1507, "密钥激活次数已用完"),
    KEY_NON_USES(1508, "密钥功能使用次数已用完"),

    KEY_ALREADY_ACTIVATED(1509,"密钥已在其他设备激活"),

    SERVICE_UNAVAILABLE(2001, "系统维护中"),
    UPDATE_REQUIRED(2002, "强制更新版本");


    BizStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return this.code;
    }

    public String msg() {
        return this.msg;
    }

    @Nullable
    public static BizStatus get(int code) {
        for (BizStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态码信息
     */
    private final String msg;

}