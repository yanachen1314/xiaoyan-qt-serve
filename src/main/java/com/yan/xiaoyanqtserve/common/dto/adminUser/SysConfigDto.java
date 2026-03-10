package com.yan.xiaoyanqtserve.common.dto.adminUser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * @author yanchen
 * @since 2025/11/1 13:12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SysConfigDto {
    /**
     * 配置项 Key，如 system.email、dashscope.apiKey
     */
    @NotBlank(message = "配置项的键不能为空")
    private String configKey;

    /**
     * 配置值
     */
    @NotBlank(message = "配置项的值不能为空")
    private String configValue;

    /**
     * 分组，如 system、dashscope，默认为 system
     */
    @Builder.Default
    private String configGroup = "system";

    /**
     * 类型：string、int、boolean、json（只能为枚举 ConfigType 允许的取值）
     */
    @NotBlank(message = "配置项值类型不能为空")
    @Pattern(regexp = "^(string|int|boolean|json)$", message = "配置项值类型必须是 string、int、boolean 或 json")
    private String configType;

    /**
     * 说明/备注
     */
    private String remark;

}
