package com.yan.xiaoyanqtserve.util;

import com.yan.xiaoyanqtserve.common.Constants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.SignatureAlgorithm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 密钥生成工具类（非对称加密算法）
 *
 * @author yanchen
 * @since 2025/5/14 23:32
 */
public class KeysUtil {
    /**
     * 签名算法，默认使用 RSA256，详情见：{@link Jwts.SIG}
     */
    private static final SignatureAlgorithm ALG = Jwts.SIG.RS256;
    // 密钥保存目录
    private static final String KEY_DIR = System.getProperty("user.dir")+"src/main/resources/key";
    // 默认字符集
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


    /**
     * 通过jjwt提供的方法生成密钥
     * <p>
     * 密钥保存路径：src/main/resources/key
     */
    public static void generateKey() {
        // 生成密钥对
        KeyPair keyPair = ALG.keyPair().build();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // 转换为 PEM 格式（添加头尾标识）
        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Encoders.BASE64.encode(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";

        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Encoders.BASE64.encode(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        // 确定保存路径
        Path keyDir = Paths.get(KEY_DIR);

        // 保存私钥
        Path privateKeyPath = keyDir.resolve("private.pem");
        // 保存公钥
        Path publicKeyPath = keyDir.resolve("public.pem");
        try {
            Files.createDirectories(keyDir); // 创建目录
            Files.writeString(privateKeyPath, privateKeyPem, Constants.DEFAULT_CHARSET);
            Files.writeString(publicKeyPath, publicKeyPem, Constants.DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("密钥对保存失败");
        }
    }

    // 读取私钥
    public static PrivateKey getPrivateKey() {
        String privateKeyPem;
        try {
            privateKeyPem = readPemFile("key/private.pem");
        } catch (Exception e) {
            throw new RuntimeException("读取私钥失败");
        }
        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("读取私钥失败");
        }
    }

    // 读取公钥
    public static PublicKey getPublicKey() {
        String publicKeyPem;
        try {
            publicKeyPem = readPemFile("key/public.pem");
        } catch (Exception e) {
            throw new RuntimeException("读取公钥失败");
        }
        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        try {
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("读取公钥失败");
        }
    }

    // 读取 PEM 文件并移除头尾标识
    private static String readPemFile(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        String pemContent = StreamUtils.copyToString(resource.getInputStream(), DEFAULT_CHARSET);
        return pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""); // 移除所有空白字符（包括换行）
    }

}
