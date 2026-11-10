package com.example.practicenote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/** React(Vite)からのAPI呼び出しを許可する。LAN上のスマートフォン確認にも対応 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DEFAULT_ALLOWED_ORIGINS = String.join(",",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://192.168.*.*:5173",
            "http://10.*.*.*:5173",
            "http://172.16.*.*:5173");

    private final List<String> allowedOriginPatterns;

    public WebConfig(Environment environment) {
        this.allowedOriginPatterns = Arrays.stream(environment
                        .getProperty("app.cors.allowed-origin-patterns", DEFAULT_ALLOWED_ORIGINS)
                        .split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-CSRF-TOKEN")
                .allowCredentials(true);
    }
}
