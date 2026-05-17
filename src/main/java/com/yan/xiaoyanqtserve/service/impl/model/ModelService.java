package com.yan.xiaoyanqtserve.service.impl.model;

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

    // 当前模型索引
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    // 模型列表缓存
    private volatile List<String> modelListCache;
    // 上次刷新模型列表的时间
    private volatile long lastRefreshTime = 0;
    // 模型列表缓存刷新间隔（毫秒）
    private static final long MODEL_LIST_REFRESH_INTERVAL = 5 * 60 * 1000; // 5分钟

    /**
     * 模型调用（支持失败自动重试）
     *
     * @param inputText 输入文本
     * @return 结果DTO
     */
    @Retryable(retryFor = {BizException.class}, maxAttempts = 3, // 最大尝试3次
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
                log.info("调用阿里模型服务出现错误，返回结果详情：{}", result);
                throw new BizException("没有结果，处理失败，请稍后再试！");
            }
        } catch (ALIYunModelException e) {
            // 在这里转成业务异常，触发重试
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 重试失败后的兜底方法
     */
    @Recover
    public AigcModelResultDto recover(Exception e, String inputText) {
        log.info("调用阿里模型服务多次失败，进入兜底逻辑。输入文本: {}，异常: {}", inputText, e.getMessage(), e);
        throw new BizException(formatExceptionMessage(e.getMessage()));
    }

    private String formatExceptionMessage(String message){
        if(message.contains("400") && message.contains("data may contain inappropriate content")){
            return "输入或输出文本可能包含敏感内容，请进行内容合规检查，修改输入后重试。";
        }
        if(message.contains("400") && message.contains("APIConnectionError")){
            return "本地网络问题，请检查网络设置。";
        }
        if(message.contains("429")){
            return "请求次数过多，请稍后再试。";
        }
        return message;
    }

    /**
     * 获取模型列表，优先使用缓存，定期刷新
     *
     * @return 模型列表
     */
    private List<String> getCachedModelList() {
        long currentTime = System.currentTimeMillis();

        // 检查缓存是否有效
        if (modelListCache != null && (currentTime - lastRefreshTime) <= MODEL_LIST_REFRESH_INTERVAL) {
            return modelListCache;
        }

        synchronized (this) {
            // 双重检查
            if (modelListCache == null || (currentTime - lastRefreshTime) > MODEL_LIST_REFRESH_INTERVAL) {
                try {
                    log.debug("刷新模型列表缓存");
                    List<String> newModelList = sysConfigService.getModelList();

                    // 只有当新列表不为空时才更新缓存
                    if (newModelList != null && !newModelList.isEmpty()) {
                        modelListCache = newModelList;
                        lastRefreshTime = currentTime;
                    } else {
                        log.info("获取到空的模型列表，保持原有缓存");
                        lastRefreshTime = currentTime; // 更新时间避免频繁重试
                    }
                } catch (Exception e) {
                    log.info("刷新模型列表缓存失败", e);
                    // 发生异常时不更新缓存，避免丢失有效数据
                    lastRefreshTime = currentTime; // 避免频繁重试
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
     * @throws ALIYunModelException 模型调用异常
     */
    private GenerationResult callWithModelByPolling(String text) throws ALIYunModelException {
        if (text == null || text.trim().isEmpty()) {
            throw new ALIYunModelException("输入文本不能为空");
        }
        // 1. 获取百炼API Key
        String dashscopeApiKey = sysConfigService.getValue(Constants.DASHSCOPE_API_KEY, ConfigType.STRING,
                String.class);
        if (dashscopeApiKey == null || dashscopeApiKey.trim().isEmpty()) {
            throw new ALIYunModelException("缺少 DashScope API Key");
        }

        // 2. 从缓存获取模型列表
        List<String> modelList = getCachedModelList();
        if (modelList == null || modelList.isEmpty()) {
            // 如果没有配置模型列表，抛出异常
            throw new ALIYunModelException("未配置轮询模型列表");
        }

        // 3. 轮询选择模型
        String selectedModelName;
        int size = modelList.size();
        if (size == 1) {
            selectedModelName = modelList.get(0);
            log.debug("仅配置了一个模型，使用: {}", selectedModelName);
        } else {
            // 使用AtomicInteger的getAndIncrement方法，线程安全地获取下一个索引
            int index = currentIndex.getAndIncrement();
            // 处理负数情况（Integer.MAX_VALUE 后溢出），确保在 [0, size) 范围内
            int actualIndex = Math.floorMod(index, size);
            selectedModelName = modelList.get(actualIndex);
            log.debug("【轮询】选择模型: {} (索引: {})", selectedModelName, actualIndex);
        }
        return callWithSingleModel(text, dashscopeApiKey, selectedModelName);
    }

    /**
     * 使用指定的 API Key 和模型名称调用模型
     */
    private GenerationResult callWithSingleModel(String text, String apiKey, String modelName)
            throws ALIYunModelException {
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
            throw new ALIYunModelException(e.getMessage());
        } catch (NoApiKeyException e) {
            throw new ALIYunModelException("缺少 DashScope API Key");
        } catch (InputRequiredException e) {
            throw new ALIYunModelException("输入文本不能为空");
        }
    }

    private static class ALIYunModelException extends Exception {
        public ALIYunModelException(String message) {
            super(message);
        }
    }

}
