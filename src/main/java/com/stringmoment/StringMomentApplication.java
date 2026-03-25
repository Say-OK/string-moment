package com.stringmoment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // 开启定时任务
@SpringBootApplication
public class StringMomentApplication {

    public static void main(String[] args) {
        SpringApplication.run(StringMomentApplication.class, args);
    }

}
