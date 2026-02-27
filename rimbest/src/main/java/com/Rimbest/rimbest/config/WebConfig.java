package com.Rimbest.rimbest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurer le gestionnaire de ressources pour les fichiers téléchargés
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./" + uploadDir + "/");
    }
}