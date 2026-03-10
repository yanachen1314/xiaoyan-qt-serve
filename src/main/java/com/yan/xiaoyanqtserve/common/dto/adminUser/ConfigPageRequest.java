package com.yan.xiaoyanqtserve.common.dto.adminUser;

import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * 配置项分页请求参数
 *
 * @author yanchen
 * @since 2025/9/2 18:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConfigPageRequest extends PageRequest {
    /**
     * 配置项key，例如：dashscope.apiKey
     */
    private String configKey;
    /**
     * 配置项分组，例如：dashscope
     */
    private String configGroup;

    /**
     * 配置项值类型，默认为string
     * <p>
     * 允许以下几种类型：string、int、boolean、json
     * @see com.yan.xiaoyanqtserve.common.enums.ConfigType
     */
    @Pattern(regexp = "^(string|int|boolean|json)$", message = "配置项值类型必须是 string、int、boolean 或 json")
    private String configType = "string";

}
