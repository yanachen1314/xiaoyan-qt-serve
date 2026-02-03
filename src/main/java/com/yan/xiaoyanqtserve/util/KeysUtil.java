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
    // 默认字符集
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 可配置的密钥路径相关参数
    private static final String KEY_RELATIVE_PATH = "key"; // 相对于 resources 的路径
    private static final String PRIVATE_KEY_FILENAME = "private.pem";
    private static final String PUBLIC_KEY_FILENAME = "public.pem";

    /**
     * 获取密钥目录的完整路径
     */
    private static Path getKeyDirectoryPath() {
        String projectRoot = System.getProperty("user.dir");
        return Paths.get(projectRoot, "src", "main", "resources", KEY_RELATIVE_PATH);
    }
    /**
     * 获取相对于 classpath 的密钥路径
     */
    private static String getClasspathKeyPath(String filename) {
        return KEY_RELATIVE_PATH + "/" + filename;
    }


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
        Path keyDirectoryPath = getKeyDirectoryPath();

        // 保存私钥
        Path privateKeyPath = keyDirectoryPath.resolve(PRIVATE_KEY_FILENAME);
        // 保存公钥
        Path publicKeyPath = keyDirectoryPath.resolve(PUBLIC_KEY_FILENAME);
        try {
            Files.createDirectories(keyDirectoryPath); // 创建目录
            Files.writeString(privateKeyPath, privateKeyPem, Constants.DEFAULT_CHARSET);
            Files.writeString(publicKeyPath, publicKeyPem, Constants.DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("密钥对保存失败");
        }
    }

    // 读取私钥
    public static PrivateKey getPrivateKey() {
        String privateKeyPem = readKeyFile(PRIVATE_KEY_FILENAME);

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
        String publicKeyPem = readKeyFile(PUBLIC_KEY_FILENAME);

        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        try {
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("读取公钥失败");
        }
    }

    /**
     * 统一的密钥文件读取方法
     */
    private static String readKeyFile(String filename) {
        // 首先尝试从 classpath 读取
        try {
            return readPemFromClasspath(filename);
        } catch (Exception e) {
            // 如果从 classpath 读取失败，尝试从项目目录读取
            try {
                return readPemFromFileSystem(filename);
            } catch (Exception ex) {
                throw new RuntimeException("读取密钥文件失败: " + filename, ex);
            }
        }
    }

    /**
     * 从 classpath 读取 PEM 文件
     */
    private static String readPemFromClasspath(String filename) throws Exception {
        String classpath = getClasspathKeyPath(filename);
        ClassPathResource resource = new ClassPathResource(classpath);
        if (!resource.exists()) {
            throw new Exception("Resource not found: " + classpath);
        }
        String pemContent = StreamUtils.copyToString(resource.getInputStream(), DEFAULT_CHARSET);
        return removePemHeaders(pemContent);
    }

    /**
     * 从文件系统读取 PEM 文件（用于开发环境）
     */
    private static String readPemFromFileSystem(String filename) throws IOException {
        Path fullPath = getKeyDirectoryPath().resolve(filename);
        String pemContent = Files.readString(fullPath, DEFAULT_CHARSET);
        return removePemHeaders(pemContent);
    }

    /**
     * 移除 PEM 头尾标识和空白字符
     */
    private static String removePemHeaders(String pemContent) {
        return pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""); // 移除所有空白字符（包括换行）
    }

}
