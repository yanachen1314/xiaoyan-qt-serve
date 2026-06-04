package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 微信公众号安全模式消息体加解密服务。
 */
@Service
@RequiredArgsConstructor
public class WechatCryptoService {
    private static final int RANDOM_BYTES_LENGTH = 16;
    private static final int MESSAGE_LENGTH_BYTES = 4;
    private static final int PKCS7_BLOCK_SIZE = 32;

    private final WechatPropertiesConfig wechatPropertiesConfig;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 解密微信安全模式下 XML 中的 Encrypt 密文。
     *
     * @param encrypt 微信 XML 中的 Encrypt 密文
     * @return 解密后的原始消息 XML
     */
    public String decrypt(String encrypt) {
        try {
            byte[] aesKey = getAesKey();
            byte[] cipherData = Base64.getDecoder().decode(encrypt);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesKey, 0, 16));

            byte[] plainData = removePkcs7Padding(cipher.doFinal(cipherData));
            if (plainData.length < RANDOM_BYTES_LENGTH + MESSAGE_LENGTH_BYTES) {
                throw new IllegalArgumentException("微信加密消息体长度非法");
            }

            int xmlLength = ByteBuffer.wrap(plainData, RANDOM_BYTES_LENGTH, MESSAGE_LENGTH_BYTES).getInt();
            int xmlStart = RANDOM_BYTES_LENGTH + MESSAGE_LENGTH_BYTES;
            int xmlEnd = xmlStart + xmlLength;
            if (xmlLength < 0 || xmlEnd > plainData.length) {
                throw new IllegalArgumentException("微信加密消息体中的 XML 长度非法");
            }

            String xml = new String(plainData, xmlStart, xmlLength, StandardCharsets.UTF_8);
            String appId = new String(plainData, xmlEnd, plainData.length - xmlEnd, StandardCharsets.UTF_8);
            if (!wechatPropertiesConfig.getAppId().equals(appId)) {
                throw new IllegalArgumentException("微信加密消息体 AppID 校验失败");
            }
            return xml;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("微信加密消息体解密失败", e);
        }
    }

    /**
     * 将微信被动回复明文 XML 加密为 Encrypt 密文。
     *
     * @param plainXml 明文 XML
     * @return Encrypt 密文
     */
    public String encrypt(String plainXml) {
        if (!StringUtils.hasText(plainXml)) {
            throw new IllegalArgumentException("微信回复明文不能为空");
        }

        try {
            byte[] aesKey = getAesKey();
            byte[] randomBytes = new byte[RANDOM_BYTES_LENGTH];
            secureRandom.nextBytes(randomBytes);

            byte[] xmlBytes = plainXml.getBytes(StandardCharsets.UTF_8);
            byte[] appIdBytes = wechatPropertiesConfig.getAppId().getBytes(StandardCharsets.UTF_8);
            byte[] xmlLengthBytes = ByteBuffer.allocate(MESSAGE_LENGTH_BYTES).putInt(xmlBytes.length).array();
            byte[] plainData = concat(randomBytes, xmlLengthBytes, xmlBytes, appIdBytes);
            byte[] paddedData = addPkcs7Padding(plainData);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesKey, 0, 16));
            return Base64.getEncoder().encodeToString(cipher.doFinal(paddedData));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("微信回复消息加密失败", e);
        }
    }

    /**
     * 构造微信安全模式下的密文请求 XML，主要用于测试和内部校验。
     *
     * @param plainXml 明文 XML
     * @return 仅包含 Encrypt 节点的微信 XML
     */
    public String buildEncryptedRequestXml(String plainXml) {
        String encrypt = encrypt(plainXml);
        return """
                <xml>
                    <Encrypt><![CDATA[%s]]></Encrypt>
                </xml>""".formatted(encrypt);
    }

    /**
     * 构造微信安全模式下的密文被动回复 XML。
     *
     * @param plainXml 明文回复 XML
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @return 包含 Encrypt、MsgSignature、TimeStamp、Nonce 的回复 XML
     */
    public String buildEncryptedReplyXml(String plainXml, String timestamp, String nonce) {
        String encrypt = encrypt(plainXml);
        String msgSignature = buildMessageSignature(encrypt, timestamp, nonce);
        return """
                <xml>
                    <Encrypt><![CDATA[%s]]></Encrypt>
                    <MsgSignature><![CDATA[%s]]></MsgSignature>
                    <TimeStamp>%s</TimeStamp>
                    <Nonce><![CDATA[%s]]></Nonce>
                </xml>""".formatted(encrypt, msgSignature, timestamp, nonce);
    }

    /**
     * 从微信 XML 中读取 Encrypt 节点内容。
     *
     * @param xmlBody 微信 XML
     * @return Encrypt 密文
     */
    public String extractEncrypt(String xmlBody) {
        if (!StringUtils.hasText(xmlBody)) {
            throw new IllegalArgumentException("微信加密消息体不能为空");
        }

        try {
            Document document = buildDocument(xmlBody);
            Node node = document.getDocumentElement().getElementsByTagName("Encrypt").item(0);
            if (node == null || !StringUtils.hasText(node.getTextContent())) {
                throw new IllegalArgumentException("微信加密消息体缺少 Encrypt 节点");
            }
            return node.getTextContent().trim();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalArgumentException("微信加密消息体不是合法 XML", e);
        }
    }

    private byte[] getAesKey() {
        if (!StringUtils.hasText(wechatPropertiesConfig.getAppId())
                || !StringUtils.hasText(wechatPropertiesConfig.getEncodingAesKey())) {
            throw new IllegalArgumentException("微信加密配置不完整");
        }
        if (wechatPropertiesConfig.getEncodingAesKey().length() != 43) {
            throw new IllegalArgumentException("微信 EncodingAESKey 长度必须为 43 位");
        }

        byte[] aesKey = Base64.getDecoder().decode(wechatPropertiesConfig.getEncodingAesKey() + "=");
        if (aesKey.length != 32) {
            throw new IllegalArgumentException("微信 EncodingAESKey 解码后长度必须为 32 字节");
        }
        return aesKey;
    }

    private byte[] addPkcs7Padding(byte[] data) {
        int padding = PKCS7_BLOCK_SIZE - (data.length % PKCS7_BLOCK_SIZE);
        if (padding == 0) {
            padding = PKCS7_BLOCK_SIZE;
        }

        byte[] result = Arrays.copyOf(data, data.length + padding);
        Arrays.fill(result, data.length, result.length, (byte) padding);
        return result;
    }

    private byte[] removePkcs7Padding(byte[] data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("微信加密消息体为空");
        }

        int padding = data[data.length - 1] & 0xff;
        if (padding < 1 || padding > PKCS7_BLOCK_SIZE || padding > data.length) {
            throw new IllegalArgumentException("微信加密消息体 Padding 非法");
        }

        for (int i = data.length - padding; i < data.length; i++) {
            if ((data[i] & 0xff) != padding) {
                throw new IllegalArgumentException("微信加密消息体 Padding 非法");
            }
        }
        return Arrays.copyOf(data, data.length - padding);
    }

    private byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private String buildMessageSignature(String encrypt, String timestamp, String nonce) {
        String[] parts = {
                wechatPropertiesConfig.getToken(),
                timestamp,
                nonce,
                encrypt
        };
        Arrays.sort(parts);
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-1");
            return java.util.HexFormat.of().formatHex(
                    digest.digest(String.join("", parts).getBytes(StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-1 算法", e);
        }
    }

    private Document buildDocument(String xmlBody) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });
        return builder.parse(new InputSource(new StringReader(xmlBody)));
    }
}
