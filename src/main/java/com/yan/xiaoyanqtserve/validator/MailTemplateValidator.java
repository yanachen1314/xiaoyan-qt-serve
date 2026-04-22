package com.yan.xiaoyanqtserve.validator;

import com.yan.xiaoyanqtserve.common.enums.MailTemplateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailTemplateValidator implements CommandLineRunner {

    private final ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        validateTemplates();
    }

    /**
     * 校验邮件模板文件是否存在
     * @see MailTemplateEnum 邮件模板枚举
     * @throws IllegalStateException 如果模板文件不存在
     */
    private void validateTemplates() {

        for (MailTemplateEnum template : MailTemplateEnum.values()) {
            String path = "classpath:templates/mail/" + template.getName() + ".html";
            Resource resource = resourceLoader.getResource(path);
            
            if (!resource.exists()) {
                throw new IllegalStateException(
                    String.format("🚨 严重错误：邮件模板文件缺失！枚举 [%s] 找不到文件 [%s]", 
                        template.name(), path)
                );
            }
        }
        log.info("✅ 邮件模板名称一致性校验通过，共 {} 个模板。", MailTemplateEnum.values().length);
    }
}