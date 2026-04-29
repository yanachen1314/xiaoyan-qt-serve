package com.yan.xiaoyanqtserve.aop;

import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.service.captcha.AliyunCaptchaService;
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

    @Around("@annotation(verifyCaptcha)")
    public Object validateCaptcha(ProceedingJoinPoint joinPoint,
                                  VerifyCaptcha verifyCaptcha) throws Throwable {

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"请求参数非法");
        }

        String captchaParam = request.getHeader(verifyCaptcha.headerName());

        if (!StringUtils.hasText(captchaParam) || captchaParam.length() > 500) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码参数非法");
        }

        SceneIdEnum sceneIdEnum = verifyCaptcha.sceneId();
        if (sceneIdEnum == null) {
            throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码场景ID不能为空");
        }
        if (StringUtils.hasText(captchaParam)) {
            boolean verified = aliyunCaptchaService.verifyCaptcha(sceneIdEnum, captchaParam);
            if (!verified){
                throw new BizException(BizStatus.ALI_CAPTCHA_VERIFY_FAIL,"验证码验证失败");
            } else {
                log.debug("验证码验证成功");
            }
        }

        // 验证通过，执行原方法
        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}