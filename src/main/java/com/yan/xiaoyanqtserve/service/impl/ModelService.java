package com.yan.xiaoyanqtserve.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcModelResultDto;
import com.yan.xiaoyanqtserve.common.enums.ConfigType;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yanchen
 * @since 2025/7/17 15:01
 */
@Slf4j
@Service
public class ModelService {

    @Resource
    private SysConfigService sysConfigService;

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    /**
     * 模型列表缓存
     */
    private volatile List<String> modelListCache;

    /**
     * 上次刷新模型列表的时间
     */
    private volatile long lastRefreshTime = 0;

    /**
     * 模型列表缓存刷新间隔（毫秒）
     */
    private static final long MODEL_LIST_REFRESH_INTERVAL = 5 * 60 * 1000; // 5分钟

    /**
     * 模型调用（支持失败自动重试）
     *
     * @param inputText 输入文本
     * @return 结果DTO
     */
    @Retryable(retryFor = { BizException.class }, maxAttempts = 3, // 最大尝试3次
            backoff = @Backoff(delay = 200, multiplier = 2) // 延迟200毫秒，每次延迟时间递增为2倍
    )
    public AigcModelResultDto chatModel(String inputText) {
        try {
            GenerationResult result = callWithModelByPolling(inputText);
            List<GenerationOutput.Choice> choiceList = result.getOutput().getChoices();

            if (choiceList != null && !choiceList.isEmpty()) {
                AigcModelResultDto dto = new AigcModelResultDto();
                dto.setInputText(inputText);
                dto.setOutputText(choiceList.get(0).getMessage().getContent());
                dto.setInputTokens(result.getUsage().getInputTokens());
                dto.setOutputTokens(result.getUsage().getOutputTokens());
                dto.setTotalTokens(result.getUsage().getTotalTokens());
                return dto;
            } else {
                log.error("调用阿里模型服务出现错误，返回结果详情：{}", result);
                throw new BizException("没有结果，处理失败，请稍后再试！");
            }
        } catch (NoModelConfiguredException e) {
            // 在这里转成业务异常，避免 throws 给调用方
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 重试失败后的兜底方法
     */
    @Recover
    public AigcModelResultDto recover(Exception e, String inputText) {
        log.error("调用阿里模型服务多次失败，进入兜底逻辑。输入文本: {}，异常: {}", inputText, e.getMessage(), e);
        throw new BizException(e.getMessage());
    }

    /**
     * 获取模型列表，优先使用缓存，定期刷新
     * 
     * @return 模型列表
     */
    private List<String> getCachedModelList() {
        long currentTime = System.currentTimeMillis();

        // 检查是否需要刷新缓存
        if (modelListCache == null || currentTime - lastRefreshTime > MODEL_LIST_REFRESH_INTERVAL) {
            synchronized (this) {
                // 双重检查锁定，确保只有一个线程刷新缓存
                if (modelListCache == null || currentTime - lastRefreshTime > MODEL_LIST_REFRESH_INTERVAL) {
                    log.debug("刷新模型列表缓存");
                    modelListCache = sysConfigService.getModelList();
                    lastRefreshTime = currentTime;
                }
            }
        }

        return modelListCache;
    }

    /**
     * 模型服务调用- 支持轮询调用
     *
     * @param text 处理前的文本
     * @return 模型结果
     * @throws NoModelConfiguredException 模型调用异常
     */
    private GenerationResult callWithModelByPolling(String text) throws NoModelConfiguredException {
        if (text == null || text.trim().isEmpty()) {
            throw new NoModelConfiguredException("输入文本不能为空");
        }
        // 1. 获取百炼API Key
        String dashscopeApiKey = sysConfigService.getValue(Constants.DASHSCOPE_API_KEY, ConfigType.STRING,
                String.class);
        if (dashscopeApiKey == null || dashscopeApiKey.trim().isEmpty()) {
            throw new NoModelConfiguredException("缺少 DashScope API Key");
        }

        // 2. 从缓存获取模型列表
        List<String> modelList = getCachedModelList();
        if (modelList == null || modelList.isEmpty()) {
            // 如果没有配置模型列表，抛出异常
            throw new NoModelConfiguredException("未配置轮询模型列表");
        }

        // 3. 轮询选择模型
        int size = modelList.size();
        if (size == 1) {
            String modelName = modelList.get(0);
            log.debug("仅配置了一个模型，使用: {}", modelName);
            return callWithSingleModel(text, dashscopeApiKey, modelName);
        }

        // 使用AtomicInteger的getAndIncrement方法，线程安全地获取下一个索引
        int index = currentIndex.getAndIncrement();
        if (index < 0) { // 防止整数溢出导致负数
            index = Math.abs(index);
        }
        int actualIndex = index % size;
        String selectedModelName = modelList.get(actualIndex);
        log.debug("【轮询】选择模型: {} (索引: {})", selectedModelName, actualIndex);

        return callWithSingleModel(text, dashscopeApiKey, selectedModelName);
    }

    /**
     * 使用指定的 API Key 和模型名称调用模型
     */
    private GenerationResult callWithSingleModel(String text, String apiKey, String modelName)
            throws NoModelConfiguredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("请将下列内容重写，使其更自然、更贴近人类写作风格：" + text)
                .build();
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(modelName)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        try {
            return gen.call(param);
        } catch (ApiException e) {
            throw new NoModelConfiguredException(e.getMessage());
        } catch (NoApiKeyException e) {
            throw new NoModelConfiguredException("缺少 DashScope API Key");
        } catch (InputRequiredException e) {
            throw new NoModelConfiguredException("输入文本不能为空");
        }
    }

    private static class NoModelConfiguredException extends Exception {
        public NoModelConfiguredException(String message) {
            super(message);
        }
    }

}
