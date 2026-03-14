package com.yan.xiaoyanqtserve.controller.admin;

import com.yan.xiaoyanqtserve.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统全局参数配置表
 *
 * @author 烟尘
 * @since 2026-03-10
 */
@RestController
@RequestMapping("/api/sysConfig")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;
}
