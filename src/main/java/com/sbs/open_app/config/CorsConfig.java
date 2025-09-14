package com.sbs.open_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed.origins:}")
    private String[] allowedOrigins;
    
    /**
     * Configuración global de CORS a nivel de controladores
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configuración específica para endpoints de API
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
        
        // Configuración para endpoints de amistades (más específica)
        registry.addMapping("/api/amistades/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
                .allowCredentials(false)
                .maxAge(1800);
    }
    
    @Bean
    @Profile("dev") // Para desarrollo
    public CorsConfigurationSource corsConfigurationSourceDev() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configuración permisiva para desarrollo
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Aplicar a todos los endpoints
        source.registerCorsConfiguration("/**", configuration);
        
        // Configuración específica para API
        CorsConfiguration apiConfig = new CorsConfiguration(configuration);
        apiConfig.setMaxAge(1800L); // Cache más corto para API
        source.registerCorsConfiguration("/api/**", apiConfig);
        
        return source;
    }
    
    @Bean
    @Profile("prod") // Para producción
    public CorsConfigurationSource corsConfigurationSourceProd() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos en producción
        List<String> origins = allowedOrigins.length > 0 ? 
            Arrays.asList(allowedOrigins) : 
            Arrays.asList(
                "https://tu-dominio-produccion.com",
                "https://www.tu-dominio-produccion.com",
                "https://app.tu-dominio-produccion.com"
            );
        
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Content-Type", 
            "Accept",
            "Authorization",
            "X-Requested-With",
            "X-Auth-Token"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Bean adicional para configuración de desarrollo local
     */
    @Bean
    @Profile("local")
    public CorsConfigurationSource corsConfigurationSourceLocal() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configuración muy permisiva para desarrollo local
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*.*:*",
            "http://10.*.*.*:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(86400L); // 24 horas para desarrollo local
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}