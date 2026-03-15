package com.yan.xiaoyanqtserve.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcModelResultDto;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcResponseDto;
import com.yan.xiaoyanqtserve.common.utils.MainUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author yanchen
 * @since 2026/3/10 22:43
 */
@Slf4j
@Service
public class AigcProcessingService {
    @Resource
    private ModelService modelService;

    // 文本段落最大字符数
    private static final Integer AIGC_MAX_CHAR_INPUT = 1000;
    // 段落最小字符数
    private static final Integer AIGC_MIN_CHAR_INPUT = 10;

    /**
     * 文本处理（支持多个段落）
     * @param content     待处理的内容（包含多个段落）
     * @return 处理后的结果
     */
    public AigcResponseDto dealText(String content) {
        // 将内容分割成段落列表
        List<String> paragraphs = splitParagraphs(content);
        if (paragraphs.isEmpty()) {
            throw new RuntimeException("没有有效的段落内容！");
        }

        // 计算总字符数（总消耗点币）
        Integer totalCharCount = paragraphs.stream()
                .mapToInt(MainUtil::accurateCharCount)
                .sum();
        // 验证输入文本（最大1000）
        if (totalCharCount > AIGC_MAX_CHAR_INPUT) {
            throw new RuntimeException(
                    "输入字符非法，仅允许" + AIGC_MIN_CHAR_INPUT + "-" + AIGC_MAX_CHAR_INPUT + "字");
        }
        if (totalCharCount < AIGC_MIN_CHAR_INPUT) {
            throw new RuntimeException(
                    "输入字符非法，仅允许" + AIGC_MIN_CHAR_INPUT + "-" + AIGC_MAX_CHAR_INPUT + "字");
        }

        // 使用模型处理文本，返回处理结果
        AigcModelResultDto result = processTextByModel(content, paragraphs);

        AigcResponseDto build = AigcResponseDto.builder().inputChars(totalCharCount).build();
        BeanUtils.copyProperties(result, build);
        // 处理成功，返回结果
        return build;
    }

    /**
     * 使用模型处理文本
     * @param content 输入的原始文本
     * @param paragraphs 原始文本的段落列表
     * @return 处理后的结果
     */
    @NotNull
    private AigcModelResultDto processTextByModel(String content, List<String> paragraphs) {
        // 并行处理每个段落
        List<AigcModelResultDto> modelResults = paragraphs.parallelStream()
                .map(modelService::chatModel)
                .toList();

        // 整合结果
        String outputText = modelResults.stream()
                .map(AigcModelResultDto::getOutputText)
                .collect(Collectors.joining("\n\n"));

        int totalInputTokens = modelResults.stream()
                .mapToInt(AigcModelResultDto::getInputTokens)
                .sum();

        int totalOutputTokens = modelResults.stream()
                .mapToInt(AigcModelResultDto::getOutputTokens)
                .sum();

        // 创建整合后的模型结果
        AigcModelResultDto integratedResult = new AigcModelResultDto();
        integratedResult.setInputText(content);
        integratedResult.setOutputText(outputText);
        integratedResult.setInputTokens(totalInputTokens);
        integratedResult.setOutputTokens(totalOutputTokens);
        integratedResult.setTotalTokens(totalInputTokens + totalOutputTokens);
        return integratedResult;
    }

    /**
     * 将内容分割成段落列表
     *
     * @param content 原始内容
     * @return 段落列表
     */
    private List<String> splitParagraphs(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 使用正则表达式分割段落，支持多种分隔方式
        // \n\n: 连续换行符，\r\n\r\n: Windows换行，多个空白字符等
        String[] paragraphs = content.split("\\r?\\n|\\r");
        // 过滤空段落并去除小于50字符的段落
        return Arrays.stream(paragraphs)
                .map(String::trim)
                .filter(p -> MainUtil.accurateCharCount(p) > AIGC_MIN_CHAR_INPUT)
                .collect(Collectors.toList());
    }


}
