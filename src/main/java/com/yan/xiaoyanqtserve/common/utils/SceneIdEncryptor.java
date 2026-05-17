package com.yan.xiaoyanqtserve.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * SceneId 加密工具类
 */
public class SceneIdEncryptor {

    // IV 固定为 16 字节 (AES block size)
    private static final int IV_LENGTH_BYTES = 16;

    /**
     * 对 sceneId 进行加密，生成 EncryptedSceneId
     *
     * @param sceneId   验证码业务标识（原 CaptchaAppid）
     * @param ekeyStr   控制台获取的 ekey（作为密钥基础）
     * @param expireTimeSec  密文过期时间，单位秒，范围 1~86400
     * @return Base64   编码的字符串：EncryptedSceneId（IV + 加密数据）
     * @throws Exception   加密过程中可能出现异常（如不支持的算法）
     */
    public static String encryptSceneId(String sceneId, String ekeyStr, int expireTimeSec) throws Exception {
        if (expireTimeSec <= 0 || expireTimeSec > 86400) {
            throw new IllegalArgumentException("expireTimeSec must be between 1 and 86400 seconds.");
        }

        // 获取当前时间戳（秒级）
        long timestamp = Instant.now().getEpochSecond();

        // ==================== 步骤 1: 构造密钥 Key（32字节） ====================
        byte[] keyBytes = Base64.getDecoder().decode(ekeyStr);

        // ==================== 步骤 2: 构造明文并执行 AES-256-CBC 加密 ====================
        // 明文格式：sceneId&timestamp&expireTime
        String plaintext = sceneId + "&" + timestamp + "&" + expireTimeSec;
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        // 创建 Cipher 实例：AES/CBC/PKCS7Padding
        // 注意：Java 默认使用 PKCS5Padding，但其与 PKCS7 在 AES 中等价（块大小相同）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // Java 不直接支持 PKCS7Padding，但行为一致

        // 随机生成 16 字节 IV
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH_BYTES];
        random.nextBytes(iv); // 安全随机填充 IV
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 使用 32 字节密钥初始化 SecretKeySpec
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // 初始化 Cipher 为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

        // 执行加密，得到密文字节数组（即 CaptchaSceneIdEncrypted）
        byte[] encryptedBytes = cipher.doFinal(plaintextBytes);

        // ==================== 步骤 3: 拼接 IV + 密文，并进行 Base64 编码 ====================
        // 创建一个新的字节数组：前 16 字节是 IV，后面是加密内容
        byte[] result = new byte[IV_LENGTH_BYTES + encryptedBytes.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH_BYTES);
        System.arraycopy(encryptedBytes, 0, result, IV_LENGTH_BYTES, encryptedBytes.length);

        // Base64 编码，得到最终的 EncryptedSceneId
        return Base64.getEncoder().encodeToString(result);
    }
}