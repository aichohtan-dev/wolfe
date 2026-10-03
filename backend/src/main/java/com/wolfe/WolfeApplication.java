package com.wolfe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
public class WolfeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WolfeApplication.class, args);
    }
}
