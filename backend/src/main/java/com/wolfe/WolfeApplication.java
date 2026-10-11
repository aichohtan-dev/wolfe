package com.wolfe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.wolfe.config.WolfeSecurityProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(WolfeSecurityProperties.class)
@EnableMethodSecurity
@EnableScheduling
public class WolfeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WolfeApplication.class, args);
    }
}
