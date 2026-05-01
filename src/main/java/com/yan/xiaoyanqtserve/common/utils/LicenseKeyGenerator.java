package com.yan.xiaoyanqtserve.common.utils;

import com.yan.xiaoyanqtserve.config.custome.XiaoyanSecurityConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LicenseKeyGenerator {

    private static final char[] CHARSET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final int PAYLOAD_LENGTH = 24;
    private static final int CHECK_LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final String HMAC_ALGO = "HmacSHA256";

    @Resource
    private XiaoyanSecurityConfig xiaoyanSecurityConfig;

    /**
     * 生成Key
     * @return  Key
     */
    public String generateKey() {
        String payload = generatePayload();
        String check = createChecksum(payload);

        return format(payload + check);
    }

    /**
     * 批量生成Key
     *
     * @param count 生成数量
     * @return Key列表
     */
    public List<String> generateKey(int count) {

        if (count <= 0) {
            throw new IllegalArgumentException("count must > 0");
        }

        // 使用Set防止极低概率重复
        Set<String> result = new LinkedHashSet<>(count);

        while (result.size() < count) {
            result.add(generateKey());
        }

        return new ArrayList<>(result);
    }

    /**
     * 校验Key是否合法
     * @param key  Key
     * @return 是否合法
     */
    public boolean verify(String key) {
        if (key == null) return false;

        String raw = key.replace("-", "");

        if (raw.length() != 32) return false;

        String payload = raw.substring(0, PAYLOAD_LENGTH);
        String checksum = raw.substring(PAYLOAD_LENGTH);

        return checksum.equals(createChecksum(payload));
    }

    // =========================
    // Payload生成（唯一）
    // =========================
    private static String generatePayload() {

        long time = System.currentTimeMillis();
        long seq = SEQUENCE.getAndIncrement();
        long rand = RANDOM.nextLong();

        long seed = mix(time, seq, rand);

        char[] buf = new char[PAYLOAD_LENGTH];

        for (int i = 0; i < buf.length; i++) {
            seed ^= (seed << 13);
            seed ^= (seed >>> 7);
            seed ^= (seed << 17);

            buf[i] = CHARSET[(int)(Math.abs(seed) % CHARSET.length)];
        }

        return new String(buf);
    }

    // =========================
    // HMAC校验位
    // =========================
    private String createChecksum(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(
                    xiaoyanSecurityConfig.getLicense().getRawKeyBytes(),
                    HMAC_ALGO));

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            char[] result = new char[CHECK_LENGTH];

            for (int i = 0; i < CHECK_LENGTH; i++) {
                int v = hash[i] & 0xFF;
                result[i] = CHARSET[v % CHARSET.length];
            }

            return new String(result);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static long mix(long t, long s, long r) {
        long x = t ^ (s << 21);
        x ^= r;
        x ^= (x << 37);
        return x;
    }

    private static String format(String raw) {
        StringBuilder sb = new StringBuilder(35);

        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && i % 8 == 0) {
                sb.append('-');
            }
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }
}