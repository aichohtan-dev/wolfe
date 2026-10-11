package com.wolfe.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "wolfe.security")
@Validated
public record WolfeSecurityProperties(
        @NotBlank @Size(min = 32) String jwtSecret,
        @NotBlank String jwtIssuer,
        @NotBlank String jwtAudience,
        @NotBlank String jwtKid,
        String jwtKeys,
        @NotBlank String frontendOrigins,
        boolean secureCookies
) {}
