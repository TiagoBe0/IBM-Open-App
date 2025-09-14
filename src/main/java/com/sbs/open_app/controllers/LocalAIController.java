package com.sbs.open_app.controllers;

import com.sbs.open_app.servicios.LocalAIService;
import com.sbs.open_app.config.AIErrorHandlingConfig.AIErrorHandler;
import com.sbs.open_app.dto.AIResponse;
import com.sbs.open_app.dto.AIRequest;
import com.sbs.open_app.dto.CodeAnalysisRequest;
import com.sbs.open_app.dto.ModelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/local-ai")
public class LocalAIController {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalAIController.class);
    
    @Autowired
    private LocalAIService localAIService;
    
    @Autowired
    private AIErrorHandler errorHandler;
    
    /**
     * Endpoint principal para generar respuestas con IA local
     */
@PostMapping("/ask")
public ResponseEntity<Map<String, Object>> askAI(@RequestBody AIRequest request) {
    logger.info("Solicitud de IA recibida - Tipo: {}, Usuario: {}", 
               request.getTipo(), request.getUsuarioNombre());
    
    logger.debug("Prompt recibido: {}", request.getMensaje());
    
    // Validar request
    if (request.getMensaje() == null || request.getMensaje().trim().isEmpty()) {
        logger.warn("Mensaje vacío recibido");
        return ResponseEntity.badRequest().body(createErrorResponse("El mensaje no puede estar vacío", false));
    }
    
    try {
        // Generar respuesta con IA local
        AIResponse response = localAIService.generateResponse(
            request.getMensaje(), 
            buildContext(request)
        );
        
        // Log detallado de la respuesta
        if (response.isSuccess()) {
            logger.info("Respuesta de IA generada exitosamente - Tokens: {}/{} (Total: {}), Modelo: {}", 
                       response.getPromptTokens(), response.getCompletionTokens(), 
                       response.getTotalTokens(), response.getModel());
            
            logger.debug("Contenido de la respuesta:\n{}", response.getContent());
        } else {
            logger.error("Error en generación de IA: {}", response.getError());
        }
        
        // Preparar respuesta para el frontend
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", response.isSuccess());
        
        if (response.isSuccess()) {
            responseMap.put("content", response.getContent());
            responseMap.put("model", response.getModel());
            responseMap.put("inputTokens", response.getPromptTokens());
            responseMap.put("outputTokens", response.getCompletionTokens());
            responseMap.put("totalTokens", response.getTotalTokens());
            responseMap.put("estimatedCost", 0.0);
            responseMap.put("processingTime", response.getProcessingTime());
        } else {
            responseMap.put("error", response.getError());
            responseMap.put("suggestion", "Verifique que Ollama esté ejecutándose y el modelo esté disponible");
        }
        
        logger.info("Enviando respuesta al frontend: success={}, contentLength={}", 
                   response.isSuccess(), 
                   response.isSuccess() ? response.getContent().length() : "N/A");
        
        return ResponseEntity.ok(responseMap);
        
    } catch (Exception e) {
        errorHandler.logError(e, "askAI");
        logger.error("Error inesperado en askAI: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
            .body(createErrorResponse("Error procesando consulta: " + e.getMessage(), true));
    }
}
    
    /**
     * Verificar estado del modelo local
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getModelStatus() {
        logger.info("🔍 Verificando estado del modelo de IA");
        
        try {
            ModelStatus status = localAIService.checkModelStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("configured", status.isAvailable());
            response.put("available", status.isAvailable());
            response.put("modelName", status.getModelName());
            response.put("serverUrl", "http://localhost:11434");
            response.put("message", status.isAvailable() ? "Modelo disponible y listo" : status.getDetails());
            response.put("type", "local");
            
            if (status.isAvailable()) {
                logger.info("✅ Modelo de IA disponible: {}", status.getModelName());
            } else {
                logger.warn("⚠️ Modelo de IA no disponible: {}", status.getServerUrl());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error verificando estado: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error verificando estado de IA"
            ));
        }
    }
    
    /**
     * Listar modelos disponibles
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getAvailableModels() {
        logger.info("📋 Solicitando lista de modelos disponibles");
        
        try {
            List<String> models = localAIService.getAvailableModels();
            
            logger.info("📊 Modelos disponibles: {}", models);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "models", models,
                "count", models.size()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo modelos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error obteniendo modelos disponibles"
            ));
        }
    }
    
    /**
     * Endpoint para análisis de código
     */
    @PostMapping("/analyze-code")
    public ResponseEntity<Map<String, Object>> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        logger.info("🔍 Solicitud de análisis de código - Lenguaje: {}, Enfoque: {}", 
                   request.getLanguage(), request.getFocus());
        
        logger.debug("📝 Código a analizar:\n{}", request.getCode());
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            logger.warn("❌ Código vacío recibido");
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "El código no puede estar vacío"
            ));
        }
        
        try {
            String analysisPrompt = buildCodeAnalysisPrompt(request);
            AIResponse response = localAIService.generateResponse(analysisPrompt, "");
            
            // Log del análisis
            if (response.isSuccess()) {
                logger.info("✅ Análisis de código completado - Tokens: {}", response.getTotalTokens());
                logger.debug("📋 Resultado del análisis:\n{}", response.getContent());
            } else {
                logger.error("❌ Error en análisis de código: {}", response.getError());
            }
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", response.isSuccess());
            
            if (response.isSuccess()) {
                responseMap.put("analysis", response.getContent());
                responseMap.put("language", request.getLanguage());
                responseMap.put("tokensUsed", response.getTotalTokens());
                responseMap.put("processingTime", response.getProcessingTime());
            } else {
                responseMap.put("error", response.getError());
            }
            
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            logger.error("💥 Error analizando código: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error en el análisis de código"
            ));
        }
    }
    
    // Métodos auxiliares
    private Map<String, Object> createErrorResponse(String message, boolean includeSuggestion) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        if (includeSuggestion) {
            errorResponse.put("suggestion", "Intente nuevamente o verifique la conexión con Ollama");
        }
        
        return errorResponse;
    }
    
    private String buildContext(AIRequest request) {
        StringBuilder context = new StringBuilder();
        
        if (request.getUsuarioNombre() != null) {
            context.append("Usuario: ").append(request.getUsuarioNombre()).append("\n");
        }
        
        switch (request.getTipo().toLowerCase()) {
            case "help":
                context.append("Tipo: Ayuda del sistema\n");
                context.append("Contexto: Sistema web de gestión jerárquica\n");
                break;
            case "analysis":
                context.append("Tipo: Análisis de datos\n");
                context.append("Contexto: Análisis de información del dashboard\n");
                break;
            case "code":
                context.append("Tipo: Generación de código\n");
                context.append("Contexto: Desarrollo de software\n");
                break;
            default:
                context.append("Tipo: Consulta general\n");
                break;
        }
        
        return context.toString();
    }
    
    private String buildCodeAnalysisPrompt(CodeAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analiza el siguiente código ").append(request.getLanguage()).append(":\n\n");
        prompt.append("```").append(request.getLanguage()).append("\n");
        prompt.append(request.getCode());
        prompt.append("\n```\n\n");
        
        prompt.append("Proporciona un análisis que incluya:\n");
        prompt.append("1. Calidad del código y mejores prácticas\n");
        prompt.append("2. Posibles errores o problemas\n");
        prompt.append("3. Sugerencias de optimización\n");
        prompt.append("4. Comentarios sobre legibilidad\n");
        
        if (request.getFocus() != null && !request.getFocus().isEmpty()) {
            prompt.append("5. Enfoque especial en: ").append(request.getFocus()).append("\n");
        }
        
        return prompt.toString();
    }
    
    // DTOs como clases internas (opcional, puedes mantenerlas separadas)
    public static class AIRequest {
        private String mensaje;
        private String usuarioNombre;
        private String tipo = "general";
        
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        
        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
        
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }
    
    public static class CodeAnalysisRequest {
        private String code;
        private String language = "java";
        private String focus;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getFocus() { return focus; }
        public void setFocus(String focus) { this.focus = focus; }
    }
}