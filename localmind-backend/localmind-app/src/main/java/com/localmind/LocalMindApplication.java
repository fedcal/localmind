package com.localmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LocalMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalMindApplication.class, args);
    }
}
