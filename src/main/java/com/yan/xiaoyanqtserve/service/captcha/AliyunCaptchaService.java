package com.yan.xiaoyanqtserve.service.captcha;

import com.aliyun.tea.TeaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 阿里云验证码2.0封装服务（V2版）
 * @author yanchen
 * @since 2026/4/26 22:13
 */
@Slf4j
@Service
public class AliyunCaptchaService {


    public static void main(String[] args_) throws Exception {
        java.util.List<String> args = java.util.Arrays.asList(args_);
        // ====================== 1. 初始化配置 ======================
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        // 设置您的AccessKey ID 和 AccessKey Secret。
        // getEnvProperty只是个示例方法，需要您自己实现AccessKey ID 和 AccessKey Secret安全的获取方式。
        config.accessKeyId = "ACCESS_KEY_ID";
        config.accessKeySecret = "ACCESS_KEY_SECRET";
        //设置请求地址 国内调用地址 captcha.cn-shanghai.aliyuncs.com   新加坡调用地址 captcha.ap-southeast-1.aliyuncs.com
        config.endpoint = "captcha.cn-shanghai.aliyuncs.com";
        // 设置连接超时为5000毫秒
        config.connectTimeout = 5000;
        // 设置读超时为5000毫秒
        config.readTimeout = 5000;
        // ====================== 2. 初始化客户端（实际生产代码中建议复用client） ======================
        com.aliyun.captcha20230305.Client client = new com.aliyun.captcha20230305.Client(config);
        // 创建APi请求
        com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest request = new com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest();
        // 本次验证的场景ID，建议传入，防止前端被篡改场景
        request.sceneId = "本次验证的场景ID";
        // 前端传来的验证参数 CaptchaVerifyParam
        request.captchaVerifyParam = "前端传来的验证参数 CaptchaVerifyParam";
        // ====================== 3. 发起请求） ======================
        try {
            com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse resp = client.verifyIntelligentCaptcha(request);
            // 建议使用您系统中的日志组件，打印返回
            // 获取验证码验证结果（请注意判空），将结果返回给前端。出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = resp.body.result.verifyResult;
            // 原因code
            String captchaVerifyCode = resp.body.result.verifyCode;
        } catch (TeaException error) {
            // 建议使用您系统中的日志组件，打印异常
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = true;
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 建议使用您系统中的日志组件，打印异常
            // 出现异常建议认为验证通过，优先保证业务可用，然后尽快排查异常原因。
            Boolean captchaVerifyResult = true;
        }
    }
}
