package com.wolfe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {
    @Bean(name = "pdfImportExecutor")
    public TaskExecutor pdfImportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("pdf-import-");
        executor.initialize();
        return executor;
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only explicitly published catalog media is public. PDF import staging remains private/admin-only.
        registry.addResourceHandler("/catalog/published/**")
                .addResourceLocations("file:public/catalog/published/")
                .setCachePeriod(86400);
    }
}
