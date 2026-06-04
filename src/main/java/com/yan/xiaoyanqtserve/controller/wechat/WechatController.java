package com.yan.xiaoyanqtserve.controller.wechat;

import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCallbackService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCryptoService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatMessageParser;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号服务器回调接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
public class WechatController {
    private static final MediaType TEXT_PLAIN_UTF8 = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private static final MediaType APPLICATION_XML_UTF8 = new MediaType("application", "xml", StandardCharsets.UTF_8);

    private final WechatSignatureService wechatSignatureService;
    private final WechatCryptoService wechatCryptoService;
    private final WechatMessageParser wechatMessageParser;
    private final WechatCallbackService wechatCallbackService;

    /**
     * 返回微信回调服务健康检查结果。
     *
     * @return 健康检查响应
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("success", true, "service", "wechat");
    }

    /**
     * 响应微信后台服务器配置验证请求。
     *
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param echostr 微信随机回显字符串
     * @return 校验通过时返回 echostr 明文
     */
    @GetMapping("/callback")
    public ResponseEntity<?> verifyCallback(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "msg_signature", required = false) String msgSignature,
            @RequestParam(value = "encrypt_type", required = false) String encryptType,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr", required = false) String echostr) {
        if (isEncryptedRequest(encryptType, null)) {
            return verifyEncryptedCallback(msgSignature, timestamp, nonce, echostr);
        }

        List<String> missingParams = getMissingSignatureParams(signature, timestamp, nonce, echostr);
        log.info("收到微信回调验证请求");

        if (!missingParams.isEmpty()) {
            log.warn("微信回调验证缺少参数: {}", missingParams);
            return jsonError(HttpStatus.BAD_REQUEST, "缺少微信回调验证参数", missingParams);
        }

        if (!wechatSignatureService.verifySignature(signature, timestamp, nonce)) {
            log.warn("微信回调签名验证失败");
            return jsonError(HttpStatus.FORBIDDEN, "微信回调签名验证失败", null);
        }

        log.info("微信回调签名验证通过");
        return ResponseEntity.ok()
                .contentType(TEXT_PLAIN_UTF8)
                .body(echostr);
    }

    /**
     * 接收微信服务器推送的消息与事件回调。
     *
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param xmlBody XML 消息体
     * @return XML 被动回复或 success 明文
     */
    @PostMapping("/callback")
    public ResponseEntity<?> receiveCallback(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "msg_signature", required = false) String msgSignature,
            @RequestParam(value = "encrypt_type", required = false) String encryptType,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestBody(required = false) byte[] xmlBody) {
        if (isEncryptedRequest(encryptType, xmlBody)) {
            return receiveEncryptedCallback(msgSignature, timestamp, nonce, xmlBody);
        }

        List<String> missingParams = getMissingSignatureParams(signature, timestamp, nonce);
        if (!missingParams.isEmpty()) {
            log.warn("微信消息回调缺少参数: {}", missingParams);
            return jsonError(HttpStatus.BAD_REQUEST, "缺少微信回调签名参数", missingParams);
        }

        if (!wechatSignatureService.verifySignature(signature, timestamp, nonce)) {
            log.warn("微信回调签名验证失败");
            return jsonError(HttpStatus.FORBIDDEN, "微信回调签名验证失败", null);
        }

        try {
            WechatMessage message = wechatMessageParser.parse(xmlBody);
            String reply = wechatCallbackService.handleCallback(message);
            if (reply != null) {
                return ResponseEntity.ok()
                        .contentType(APPLICATION_XML_UTF8)
                        .body(reply);
            }
            return ResponseEntity.ok()
                    .contentType(TEXT_PLAIN_UTF8)
                    .body("success");
        } catch (IllegalArgumentException e) {
            log.warn("微信消息解析失败: {}", e.getMessage());
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    private ResponseEntity<?> verifyEncryptedCallback(
            String msgSignature,
            String timestamp,
            String nonce,
            String echostr) {
        List<String> missingParams = getMissingEncryptedParams(msgSignature, timestamp, nonce, echostr);
        log.info("收到微信加密回调验证请求");

        if (!missingParams.isEmpty()) {
            log.warn("微信加密回调验证缺少参数: {}", missingParams);
            return jsonError(HttpStatus.BAD_REQUEST, "缺少微信加密回调验证参数", missingParams);
        }

        if (!wechatSignatureService.verifyMessageSignature(msgSignature, echostr, timestamp, nonce)) {
            log.warn("微信加密回调签名验证失败");
            return jsonError(HttpStatus.FORBIDDEN, "微信加密回调签名验证失败", null);
        }

        String plainEcho = wechatCryptoService.decrypt(echostr);
        return ResponseEntity.ok()
                .contentType(TEXT_PLAIN_UTF8)
                .body(plainEcho);
    }

    private ResponseEntity<?> receiveEncryptedCallback(
            String msgSignature,
            String timestamp,
            String nonce,
            byte[] xmlBody) {
        List<String> missingParams = getMissingEncryptedParams(msgSignature, timestamp, nonce);
        if (!missingParams.isEmpty()) {
            log.warn("微信加密消息回调缺少参数: {}", missingParams);
            return jsonError(HttpStatus.BAD_REQUEST, "缺少微信加密回调签名参数", missingParams);
        }

        try {
            String encrypt = wechatCryptoService.extractEncrypt(toXmlString(xmlBody));
            if (!wechatSignatureService.verifyMessageSignature(msgSignature, encrypt, timestamp, nonce)) {
                log.warn("微信加密回调签名验证失败");
                return jsonError(HttpStatus.FORBIDDEN, "微信加密回调签名验证失败", null);
            }

            String plainXml = wechatCryptoService.decrypt(encrypt);
            WechatMessage message = wechatMessageParser.parse(plainXml);
            String reply = wechatCallbackService.handleCallback(message);
            if (reply != null) {
                return ResponseEntity.ok()
                        .contentType(APPLICATION_XML_UTF8)
                        .body(wechatCryptoService.buildEncryptedReplyXml(reply, timestamp, nonce));
            }
            return ResponseEntity.ok()
                    .contentType(TEXT_PLAIN_UTF8)
                    .body("success");
        } catch (IllegalArgumentException e) {
            log.warn("微信加密消息处理失败: {}", e.getMessage());
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    private boolean isEncryptedRequest(String encryptType, byte[] xmlBody) {
        return "aes".equalsIgnoreCase(encryptType)
                || (xmlBody != null && toXmlString(xmlBody).contains("<Encrypt>"));
    }

    private String toXmlString(byte[] xmlBody) {
        if (xmlBody == null || xmlBody.length == 0) {
            return "";
        }
        return new String(xmlBody, StandardCharsets.UTF_8);
    }

    private List<String> getMissingSignatureParams(String signature, String timestamp, String nonce) {
        List<String> missingParams = new ArrayList<>();
        if (!StringUtils.hasText(signature)) {
            missingParams.add("signature");
        }
        if (!StringUtils.hasText(timestamp)) {
            missingParams.add("timestamp");
        }
        if (!StringUtils.hasText(nonce)) {
            missingParams.add("nonce");
        }
        return missingParams;
    }

    private List<String> getMissingSignatureParams(String signature, String timestamp, String nonce, String echostr) {
        List<String> missingParams = getMissingSignatureParams(signature, timestamp, nonce);
        if (!StringUtils.hasText(echostr)) {
            missingParams.add("echostr");
        }
        return missingParams;
    }

    private List<String> getMissingEncryptedParams(String msgSignature, String timestamp, String nonce) {
        List<String> missingParams = new ArrayList<>();
        if (!StringUtils.hasText(msgSignature)) {
            missingParams.add("msg_signature");
        }
        if (!StringUtils.hasText(timestamp)) {
            missingParams.add("timestamp");
        }
        if (!StringUtils.hasText(nonce)) {
            missingParams.add("nonce");
        }
        return missingParams;
    }

    private List<String> getMissingEncryptedParams(
            String msgSignature,
            String timestamp,
            String nonce,
            String echostr) {
        List<String> missingParams = getMissingEncryptedParams(msgSignature, timestamp, nonce);
        if (!StringUtils.hasText(echostr)) {
            missingParams.add("echostr");
        }
        return missingParams;
    }

    private ResponseEntity<Map<String, Object>> jsonError(
            HttpStatus status,
            String message,
            List<String> missingParams) {
        if (missingParams == null) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", message));
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "success", false,
                        "message", message,
                        "missing", missingParams
                ));
    }
}
