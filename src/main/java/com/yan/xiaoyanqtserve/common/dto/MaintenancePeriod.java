package com.yan.xiaoyanqtserve.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime start;
    /**
     * 维护结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime end;

    /**
     * 维护状态是否开启
     */
    private Boolean status;

    public MaintenancePeriod(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

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
     * 获取维护时间消息
     * @return 维护时间消息
     */
    public String maintenanceMessage() {
        LocalDateTime now = LocalDateTime.now();
        if(isInMaintenance(now)){
            // 正在维护中，显示预计结束时间
            long hoursLeft = java.time.Duration.between(now, end).toHours();
            long minutesLeft = java.time.Duration.between(now, end).toMinutes() % 60;

            if (hoursLeft > 0) {
                return String.format("系统正在维护中，预计%d小时%d分钟后恢复。", hoursLeft, minutesLeft);
            } else if (minutesLeft > 0) {
                return String.format("系统正在维护中，预计%d分钟后恢复。", minutesLeft);
            } else {
                return "系统正在维护中，即将恢复。";
            }
        }
        return "服务已恢复正常。";
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