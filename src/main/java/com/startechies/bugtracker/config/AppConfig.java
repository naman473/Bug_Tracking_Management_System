package com.startechies.bugtracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 *
 * CORS: restricted to localhost for development.
 * For production, restrict to your actual front-end domain in allowedOrigins.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final ProjectOwnershipInterceptor ownershipInterceptor;

    public AppConfig(ProjectOwnershipInterceptor ownershipInterceptor) {
        this.ownershipInterceptor = ownershipInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ownershipInterceptor)
                .addPathPatterns("/manager/projects/*", "/manager/projects/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST");
    }
}
