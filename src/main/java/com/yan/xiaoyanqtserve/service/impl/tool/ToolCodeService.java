package com.yan.xiaoyanqtserve.service.impl.tool;

import com.alibaba.fastjson2.JSON;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolCodeOption;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tool_code whitelist loaded from system configuration.
 */
@Service
@RequiredArgsConstructor
public class ToolCodeService {
    private final SysConfigService sysConfigService;

    public List<ToolCodeOption> listOptions() {
        String configValue;
        try {
            configValue = sysConfigService.getValue(Constants.WECHAT_TOOL_ITEM_KEY);
        } catch (RuntimeException e) {
            throw new BizException(BizStatus.BAD_REQUEST,
                    "工具编码配置读取失败: " + Constants.WECHAT_TOOL_ITEM_KEY, e);
        }

        if (!StringUtils.hasText(configValue)) {
            throw new BizException(BizStatus.BAD_REQUEST,
                    "工具编码配置不存在: " + Constants.WECHAT_TOOL_ITEM_KEY);
        }

        List<ToolCodeOption> options;
        try {
            options = JSON.parseArray(configValue, ToolCodeOption.class);
        } catch (RuntimeException e) {
            throw new BizException(BizStatus.BAD_REQUEST,
                    "工具编码配置格式错误: " + Constants.WECHAT_TOOL_ITEM_KEY, e);
        }

        Map<String, ToolCodeOption> normalizedOptions = new LinkedHashMap<>();
        if (options != null) {
            for (ToolCodeOption option : options) {
                if (option == null || !StringUtils.hasText(option.getValue())) {
                    continue;
                }
                String value = option.getValue().trim().toLowerCase(Locale.ROOT);
                String label = StringUtils.hasText(option.getLabel()) ? option.getLabel().trim() : value;
                normalizedOptions.putIfAbsent(value, new ToolCodeOption(label, value));
            }
        }

        if (normalizedOptions.isEmpty()) {
            throw new BizException(BizStatus.BAD_REQUEST,
                    "工具编码配置为空: " + Constants.WECHAT_TOOL_ITEM_KEY);
        }

        return List.copyOf(normalizedOptions.values());
    }

    public String requireValidCode(String toolCode) {
        if (!StringUtils.hasText(toolCode)) {
            throw new BizException(BizStatus.BAD_REQUEST, "工具编码不能为空");
        }

        String normalizedToolCode = toolCode.trim().toLowerCase(Locale.ROOT);
        return listOptions().stream()
                .map(ToolCodeOption::getValue)
                .filter(normalizedToolCode::equals)
                .findFirst()
                .orElseThrow(() -> new BizException(BizStatus.BAD_REQUEST, "工具编码无效"));
    }
}
