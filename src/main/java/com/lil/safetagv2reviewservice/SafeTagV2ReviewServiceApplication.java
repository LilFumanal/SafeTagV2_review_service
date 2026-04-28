package com.lil.safetagreviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SafeTagV2ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeTagV2ReviewServiceApplication.class, args);
    }

}
