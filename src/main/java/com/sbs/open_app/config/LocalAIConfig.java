package com.sbs.open_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración para la integración con IA Local (Ollama)
 * Nota: @CrossOrigin en controladores anula esta configuración
 */
@Configuration
public class LocalAIConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalAIConfig.class);
    
    /**
     * Configurar CORS para permitir requests del frontend
     * Elimina la configuración duplicada si usas @CrossOrigin en controladores
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configurando CORS para endpoints de IA Local");
        
        registry.addMapping("/api/local-ai/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}