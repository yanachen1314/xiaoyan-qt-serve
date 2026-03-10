package com.yan.xiaoyanqtserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class XiaoyanQtServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoyanQtServeApplication.class, args);
    }

}
