package com.yan.xiaoyanqtserve.aop;

import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.captcha.AliyunCaptchaService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class CaptchaValidationAspect {
    @Resource
    private AliyunCaptchaService aliyunCaptchaService;

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 验证码校验切面
     * 根据系统配置决定是否进行验证码校验
     * @param joinPoint 切点
     * @param verifyCaptcha 验证码校验注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(verifyCaptcha)")
    public Object validateCaptcha(ProceedingJoinPoint joinPoint,
                                  VerifyCaptcha verifyCaptcha) throws Throwable {

        // 检查验证码服务是否启用
        boolean captchaEnabled;
        try {
            captchaEnabled = Boolean.TRUE.equals(sysConfigService.getCaptchaServiceEnabled());
        } catch (Exception e) {
            log.error("获取验证码服务状态失败，默认启用验证码校验", e);
            captchaEnabled = true; // 获取失败时默认启用（安全优先）
        }

        // 如果验证码服务未启用，直接执行原方法
        if (!captchaEnabled) {
            log.debug("验证码服务已禁用，跳过验证码校验");
            return joinPoint.proceed();
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"请求参数非法");
        }
        // 获取验证码参数
        String captchaParam = request.getHeader(verifyCaptcha.headerName());
        if (!StringUtils.hasText(captchaParam) || captchaParam.length() > 500) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码参数非法");
        }

        // 获取验证码场景ID，由后端设置防止前端被篡改为其他场景
        SceneIdEnum sceneIdEnum = verifyCaptcha.sceneId();
        if (sceneIdEnum == null) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码场景ID不能为空");
        }
        // 调用验证码服务进行验证
        boolean verified = aliyunCaptchaService.verifyCaptcha(sceneIdEnum, captchaParam);
        if (!verified){
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码验证失败");
        } else {
            log.debug("验证码验证成功");
        }

        // 验证通过，执行原方法
        return joinPoint.proceed();
    }

    /**
     * 获取当前HTTP请求
     * @return HttpServletRequest对象
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}