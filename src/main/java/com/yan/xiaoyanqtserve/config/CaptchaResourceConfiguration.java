package com.yan.xiaoyanqtserve.config;

import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CaptchaResourceConfiguration {

    @Bean
    public ResourceStore resourceStore() {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();
        // 使用标准化路径格式（不带尾部斜杠）
        loadResourcesFromDir(resourceStore, "tac/images", CaptchaTypeConstant.SLIDER, "default");
        return resourceStore;
    }

    private void loadResourcesFromDir(LocalMemoryResourceStore store, String dirPath, String type, String tag) {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 1. 标准化路径格式（移除可能的尾部斜杠）
            String normalizedDirPath = dirPath.endsWith("/") ?
                    dirPath.substring(0, dirPath.length() - 1) : dirPath;

            // 2. 使用正确的Ant路径模式（显式添加斜杠）
            String pattern = "classpath*:" + normalizedDirPath + "/*.{png,jpg,jpeg}";
            Resource[] resources = resolver.getResources(pattern);

            // 3. 处理空目录情况（不视为错误）
            if (resources.length == 0) {
                log.debug("目录中没有找到图片文件: {}", normalizedDirPath);
                return;
            }

            for (Resource resource : resources) {
                try {
                    // 4. 跨平台安全获取文件名
                    String filename = getSafeFilename(resource);
                    if (filename == null) continue;

                    // 5. 使用标准路径分隔符拼接路径
                    String path = normalizedDirPath.replace('\\', '/') + "/" + filename;

                    store.addResource(type, new cloud.tianai.captcha.resource.common.model.dto.Resource(
                            "classpath",
                            path,
                            tag
                    ));
                } catch (Exception e) {
                    log.warn("跳过无效资源: {}", resource.getDescription(), e);
                }
            }

            log.info("成功加载 {} 张背景图片: {}", resources.length, normalizedDirPath);

        } catch (FileNotFoundException e) {
            // 6. 目录不存在是正常情况（无图片），不视为错误
            log.debug("资源目录不存在，跳过加载: {}", dirPath);
        } catch (Exception e) {
            log.error("加载背景图失败: {}", dirPath, e);
        }
    }

    /**
     * 跨平台安全获取文件名
     * 处理Windows/Linux路径差异和特殊字符
     */
    private String getSafeFilename(Resource resource) {
        try {
            // 优先使用URL路径（跨平台兼容）
            String urlPath = resource.getURL().getPath();
            if (urlPath != null) {
                // 处理Windows路径中的盘符（如 /C:/...）
                if (urlPath.matches("^/[A-Za-z]:/.*")) {
                    urlPath = urlPath.substring(1); // 移除开头的斜杠
                }
                Path path = Paths.get(urlPath);
                return path.getFileName().toString();
            }

            // 备用方案：使用文件系统路径
            if (resource.isFile()) {
                return resource.getFile().getName();
            }

            // 最后尝试：从资源描述中提取
            String desc = resource.getDescription();
            int lastSlash = Math.max(desc.lastIndexOf('/'), desc.lastIndexOf('\\'));
            if (lastSlash >= 0 && lastSlash < desc.length() - 1) {
                return desc.substring(lastSlash + 1);
            }

        } catch (Exception e) {
            log.debug("无法解析资源路径: {}", resource.getDescription(), e);
        }
        return null;
    }
}