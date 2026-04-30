package com.yan.xiaoyanqtserve.service.impl.system;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.common.utils.VersionCheckUtil;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.common.vo.VersionComparisonResult;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 客户端版本服务
 * @author yanchen
 * @since 2026/3/31 20:45
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientVersionService {
    private final SysConfigService sysConfigService;
    private final RedisUtil redisUtil;

    /**
     * 比较版本号
     * @param clientVersion 客户端版本号
     * @return 版本比较结果对象
     */
    public VersionCheckVo compareVersions(String clientVersion) {
        // 获取最新版本号
        String latestVersion = getLatestVersion();
        // redis缓存键
        String cacheKey = "compareVersion:" + latestVersion + "-" + clientVersion;

        // 从缓存中获取比较结果
        Object cachedResult = redisUtil.get(cacheKey);
        if (cachedResult != null) {
            return (VersionCheckVo) cachedResult;
        }

        // 比较版本
        VersionComparisonResult result = VersionCheckUtil.compareVersions(clientVersion, latestVersion);

        // 构建响应
        VersionCheckVo build = VersionCheckVo.builder()
                .version(latestVersion)
                .clientVersion(clientVersion)
                .latest(result.isLatest())
                .updateRequired(result.isUpdateRequired())
                .message(result.getMessage())
                .linkUrl(sysConfigService.getValue(Constants.LINK3_KEY))
                .build();
        redisUtil.set(cacheKey, build,7, TimeUnit.DAYS);
        return build;
    }


    /**
     * 获取最新版本号
     * @return 最新版本号
     */
    public String getLatestVersion() {
        String latestVersion = sysConfigService.getValue(Constants.VERSION_KEY);
        if (latestVersion == null || latestVersion.trim().isEmpty()) {
            log.warn("后台获取最新版本号为空，使用默认版本号: v0.0.1");
            return "0.0.1";
        }
        return latestVersion;
    }
}
