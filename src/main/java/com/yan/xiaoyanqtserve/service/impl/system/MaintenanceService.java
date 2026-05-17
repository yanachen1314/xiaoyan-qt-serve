package com.yan.xiaoyanqtserve.service.impl.system;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.MaintenancePeriod;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 维护服务
 * @author yanchen
 * @since 2026/3/28 11:34
 */
@Slf4j
@Service
public class MaintenanceService {

    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 系统是否正在维护
     * @return 是否正在维护
     */
    public boolean isSystemInMaintenance() {
        MaintenancePeriod maintenancePeriod = getMaintenancePeriod();
        if (maintenancePeriod == null) {
            return false;
        }
        return maintenancePeriod.isInMaintenance(LocalDateTime.now()) && maintenancePeriod.getStatus();
    }

    /**
     * 获取维护时间
     * @return 维护时间
     */
    public MaintenancePeriod getMaintenancePeriod() {
        String valueKey = Constants.SYSTEM_MAINTENANCE_KEY;
        String value = (String) redisUtil.hGet(Constants.CONFIG_KEY, valueKey);
        String statusKey = Constants.SYSTEM_MAINTENANCE_STATUS;
        Boolean status = (Boolean) redisUtil.hGet(Constants.CONFIG_KEY, statusKey);
        // 如果Redis中不存在维护时间或状态，则从数据库中获取维护时间
        if(!StringUtils.hasText(value) || status == null) {
            List<SysConfig> configByKey = sysConfigService.getConfigByKey(
                    Constants.SYSTEM_MAINTENANCE_KEY, true, false, null);
            if (configByKey != null && !configByKey.isEmpty()) {
                value = configByKey.get(0).getConfigValue();
                status = configByKey.get(0).getStatus() == 1;
                redisUtil.hSet(Constants.CONFIG_KEY,valueKey, value);
                redisUtil.hSet(Constants.CONFIG_KEY, statusKey, status);
            }else{
                log.warn("未查询到维护时间，请检查配置");
                return null;
            }
        }

        if (StringUtils.hasText(value)) {
            MaintenancePeriod maintenancePeriod = MaintenancePeriod.parse(value);
            if (maintenancePeriod != null) {
                maintenancePeriod.setStatus(status);
                return maintenancePeriod;
            }
            log.warn("解析维护时间失败，请检查配置");
            return null;
        }
        log.warn("未查询到维护时间，请检查配置");
        return null;
    }
}
