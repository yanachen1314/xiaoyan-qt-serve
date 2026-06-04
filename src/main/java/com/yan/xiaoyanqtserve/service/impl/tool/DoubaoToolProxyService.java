package com.yan.xiaoyanqtserve.service.impl.tool;

import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.config.custome.DoubaoToolPropertiesConfig;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * 豆包 Flask 解析服务代理。
 */
@Service
public class DoubaoToolProxyService {
    private final DoubaoToolPropertiesConfig properties;
    private final RestTemplate restTemplate;

    public DoubaoToolProxyService(DoubaoToolPropertiesConfig properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                .readTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                .build();
    }

    /**
     * 代理图片解析请求。
     *
     * @param url 分享链接
     * @return Flask 响应
     */
    public ResponseEntity<String> analyzeImages(String url) {
        return post("/images/analyze", url);
    }

    /**
     * 代理视频解析请求。
     *
     * @param url 分享链接
     * @return Flask 响应
     */
    public ResponseEntity<String> analyzeVideos(String url) {
        return post("/videos/analyze", url);
    }

    private ResponseEntity<String> post(String path, String url) {
        try {
            return restTemplate.postForEntity(buildUrl(path), Map.of("url", url), String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new BizException(BizStatus.ERROR, "豆包解析服务请求失败", e);
        }
    }

    private String buildUrl(String path) {
        String baseUrl = properties.getBaseUrl();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }
}
