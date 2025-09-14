package com.sbs.open_app.servicios;

import com.sbs.open_app.config.AIErrorHandlingConfig.AIErrorHandler;
import com.sbs.open_app.dto.AIResponse;
import com.sbs.open_app.dto.ModelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class LocalAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalAIService.class);
    
    @Value("${local.ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${local.ai.model:deepseek-coder:16b}")
    private String modelName;
    
    @Value("${local.ai.enabled:true}")
    private boolean aiEnabled;
    
    @Value("${local.ai.timeout:30000}")
    private int timeout;
    
    @Value("${local.ai.temperature:0.7}")
    private double temperature;
    
    @Value("${local.ai.top-p:0.9}")
    private double topP;
    
    @Value("${local.ai.top-k:40}")
    private int topK;
    
    @Value("${local.ai.max-tokens:2048}")
    private int maxTokens;
    
    private final RestTemplate restTemplate;
    private final AIErrorHandler errorHandler;
    
    @Autowired
    public LocalAIService(AIErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Generar respuesta usando modelo local
     */
    public AIResponse generateResponse(String prompt, String context) {
        if (!aiEnabled) {
            return AIResponse.error("Servicio de IA local está deshabilitado");
        }
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return AIResponse.error("El prompt no puede estar vacío");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Generando respuesta con modelo: {} para prompt de {} caracteres", 
                       modelName, prompt.length());
            
            // Construir prompt completo
            String fullPrompt = buildOptimizedPrompt(prompt, context);
            
            // Crear request para Ollama
            Map<String, Object> request = new HashMap<>();
            request.put("model", modelName);
            request.put("prompt", fullPrompt);
            request.put("stream", false);
            request.put("options", buildModelOptions());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // Realizar llamada a Ollama
            ResponseEntity<Map> response = restTemplate.postForEntity(
                ollamaUrl + "/api/generate", 
                entity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AIResponse aiResponse = processOllamaResponse(response.getBody(), fullPrompt);
                double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
                
                return aiResponse.withProcessingTime(processingTime)
                               .addMetadata("serverUrl", ollamaUrl)
                               .addMetadata("modelUsed", modelName);
            } else {
                logger.error("Error en respuesta de Ollama: {}", response.getStatusCode());
                return AIResponse.error("Error comunicándose con el modelo local: " + response.getStatusCode(), modelName);
            }
            
        } catch (RestClientException e) {
            errorHandler.logError(e, "generateResponse");
            return AIResponse.error(errorHandler.handleOllamaError(e), modelName);
        } catch (Exception e) {
            errorHandler.logError(e, "generateResponse");
            return AIResponse.error("Error inesperado: " + e.getMessage(), modelName);
        }
    }
    
    /**
     * Verificar estado del modelo
     */
    public ModelStatus checkModelStatus() {
        try {
            // Intentar obtener lista de modelos
            ResponseEntity<Map> response = restTemplate.getForEntity(
                ollamaUrl + "/api/tags", 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                
                boolean modelFound = models != null && models.stream()
                    .anyMatch(model -> modelName.equals(model.get("name")));
                
                if (modelFound) {
                    return ModelStatus.available(modelName, ollamaUrl, "Modelo disponible y listo");
                } else {
                    String availableModels = models != null ? getModelNames(models).toString() : "ninguno";
                    return ModelStatus.unavailable(modelName, ollamaUrl, 
                        "Modelo '" + modelName + "' no encontrado. Modelos disponibles: " + availableModels);
                }
            } else {
                return ModelStatus.unavailable(modelName, ollamaUrl, 
                    "Error en respuesta del servidor: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            errorHandler.logError(e, "checkModelStatus");
            return ModelStatus.unavailable(modelName, ollamaUrl, 
                errorHandler.handleOllamaError(e));
        }
    }
    
    // Métodos auxiliares (mantienen la misma implementación)
    private String buildOptimizedPrompt(String prompt, String context) {
        StringBuilder fullPrompt = new StringBuilder();
        
        if (context != null && !context.trim().isEmpty()) {
            fullPrompt.append("Contexto: ").append(context.trim()).append("\n\n");
        }
        
        fullPrompt.append("Responde en español de manera clara y útil.\n");
        fullPrompt.append("Pregunta: ").append(prompt);
        
        return fullPrompt.toString();
    }
    
    private Map<String, Object> buildModelOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", temperature);
        options.put("top_p", topP);
        options.put("top_k", topK);
        options.put("num_predict", maxTokens);
        options.put("repeat_penalty", 1.1);
        return options;
    }
    
    private AIResponse processOllamaResponse(Map<String, Object> responseBody, String originalPrompt) {
        try {
            String content = (String) responseBody.get("response");
            
            if (content == null || content.trim().isEmpty()) {
                return AIResponse.error("Respuesta vacía del modelo", modelName);
            }
            
            // Calcular tokens (estimación aproximada)
            int promptTokens = originalPrompt.length() / 4;
            int completionTokens = content.length() / 4;
            int totalTokens = promptTokens + completionTokens;
            
            return AIResponse.success(content.trim(), modelName)
                           .withTokens(promptTokens, completionTokens)
                           .addMetadata("ollamaResponse", responseBody);
            
        } catch (Exception e) {
            logger.error("Error procesando respuesta: ", e);
            return AIResponse.error("Error procesando respuesta del modelo", modelName);
        }
    }
    
    private List<String> getModelNames(List<Map<String, Object>> models) {
        return models.stream()
            .map(model -> (String) model.get("name"))
            .filter(Objects::nonNull)
            .sorted()
            .toList();
    }
    
    /**
     * Obtener lista de modelos disponibles
     */
    public List<String> getAvailableModels() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                ollamaUrl + "/api/tags", 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                
                return models != null ? getModelNames(models) : new ArrayList<>();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error obteniendo modelos: ", e);
            return new ArrayList<>();
        }
    }
}