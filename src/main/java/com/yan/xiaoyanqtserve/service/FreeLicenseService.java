package com.yan.xiaoyanqtserve.service;

/**
 * 免费领取卡密记录服务接口
 * @author yanchen
 * @since 2026/4/22
 */
public interface FreeLicenseService {

    /**
     * 检查邮箱是否已领取过免费卡密
     *
     * @param email 邮箱地址
     * @return true-已领取，false-未领取
     */
    boolean hasReceived(String email);

    /**
     * 记录领取信息
     *
     * @param email 邮箱地址
     * @param licenseKey 卡密
     * @param deviceId 设备ID/设备指纹
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     */
    void recordLicense(String email, String licenseKey, String deviceId, String clientIp, String userAgent);
}
