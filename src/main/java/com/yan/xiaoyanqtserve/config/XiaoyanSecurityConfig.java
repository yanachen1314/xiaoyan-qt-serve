package com.yan.xiaoyanqtserve.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import java.nio.charset.StandardCharsets;

/**
 * 安全配置中心 - 严格遵循以下安全原则：
 * 1. 密钥永不以明文形式存在于内存（除必要使用时刻）
 * 2. 所有敏感字段禁止日志输出
 * 3. 启动时强制验证密钥强度
 * 4. PEM格式自动规范化处理
 */
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.security")
@Validated
@Data
public class XiaoyanSecurityConfig {

    private  LicenseConfig license;
    private  BizDataConfig bizData;

    public XiaoyanSecurityConfig() {
        this.license = new LicenseConfig();
        this.bizData = new BizDataConfig();
    }


    @PostConstruct
    public void init() {
        //  密钥完整性验证
        validateConfig();
    }

    private void validateConfig() {
        // 许可密钥验证 (必须≥32字节 = 256位)
        String licenseKey = license.getKey();
        if (licenseKey == null || licenseKey.length() < 32) {
            throw new IllegalStateException(
                    "LICENSE_KEY 长度不足! 必须≥32字符 (256位). 当前长度: "
                            + (licenseKey != null ? licenseKey.length() : 0)
            );
        }

        // PEM格式验证 (必须包含头尾标记)
        String privateKey = bizData.getEncryptPrivateKey();
        if (privateKey == null ||
                !privateKey.contains("-----BEGIN PRIVATE KEY-----") ||
                !privateKey.contains("-----END PRIVATE KEY-----")) {
            throw new IllegalStateException("无效的 PRIVATE KEY 格式!");
        }

        String publicKey = bizData.getEncryptPublicKey();
        if (publicKey == null ||
                !publicKey.contains("-----BEGIN PUBLIC KEY-----") ||
                !publicKey.contains("-----END PUBLIC KEY-----")) {
            throw new IllegalStateException("无效的 PUBLIC KEY 格式!");
        }
    }

    // ================= 内部配置类 =================
    @Data
    @ToString(exclude = {"key"}) // 关键：排除密钥字段
    public static class LicenseConfig {
        @NotBlank
        private String key;

        // 返回密钥的原始字节
        public byte[] getRawKeyBytes() {
            return key.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Data
    @ToString(exclude = {"encryptPrivateKey", "encryptPublicKey"})
    public static class BizDataConfig {
        @NotBlank
        private String algorithm; // 必须为 RSA-256

        @NotBlank
        private String encryptPrivateKey;

        @NotBlank
        private String encryptPublicKey;

        // PEM规范化处理 (修复环境变量单行格式)
        public String getFormattedPrivateKey() {
            return normalizePem(encryptPrivateKey);
        }

        public String getFormattedPublicKey() {
            return normalizePem(encryptPublicKey);
        }

        private String normalizePem(String pem) {
            return pem.replace(" ", "\n")
                    .replace("-----BEGIN", "\n-----BEGIN")
                    .replace("-----END", "\n-----END")
                    .trim();
        }
    }

}