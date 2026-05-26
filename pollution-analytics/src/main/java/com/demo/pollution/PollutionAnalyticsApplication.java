package com.demo.pollution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PollutionAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PollutionAnalyticsApplication.class, args);
    }
}
