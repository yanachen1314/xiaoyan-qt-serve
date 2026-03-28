package com.yan.xiaoyanqtserve.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 维护时间DTO
 */
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class MaintenancePeriod {
    /**
     * 维护开始时间
     */
    private LocalDateTime start;
    /**
     * 维护结束时间
     */
    private LocalDateTime end;

    /**
     * 判断当前时间是否在维护时间内
     * @param now 当前时间
     * @return 是否在维护时间内
     */
    public boolean isInMaintenance(LocalDateTime now) {
        return now != null && start != null && end != null &&
                now.isAfter(start.minusSeconds(1)) && now.isBefore(end.plusSeconds(1));
    }

    /**
     * 从JSON字符串解析维护时间
     * @param json JSON字符串
     * @return 维护时间
     */
    @Nullable
    public static MaintenancePeriod parse(String json) {
        if (!StringUtils.hasText(json)) return null;
        try {
            Map map = new ObjectMapper().readValue(json, Map.class);
            String startStr = (String) map.get("start");
            String endStr = (String) map.get("end");
            if (startStr == null || endStr == null) return null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startStr, formatter);
            LocalDateTime end = LocalDateTime.parse(endStr, formatter);
            return new MaintenancePeriod(start, end);
        } catch (Exception e) {
            log.warn("解析维护时间失败: {}", json, e);
            return null;
        }
    }
}