package com.sbs.open_app.servicios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class ClaudeService {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeService.class);
    
    @Value("${claude.api.key:}")
    private String apiKey;
    
    @Value("${claude.api.url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;
    
    @Value("${claude.api.model:claude-3-sonnet-20240229}")
    private String model;
    
    @Value("${claude.api.max-tokens:1000}")
    private int maxTokens;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ClaudeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Pregunta síncrona a Claude
     */
    public ClaudeResponse askClaude(String mensaje, String contexto) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return createErrorResponse("API Key de Claude no configurada");
        }

        try {
            // Preparar el payload
            ObjectNode payload = createClaudePayload(mensaje, contexto);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");
            
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            
            // Hacer la llamada
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseClaudeResponse(response.getBody());
            } else {
                return createErrorResponse("Error en la API de Claude: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al llamar Claude API: {}", e.getMessage());
            return createErrorResponse("Error de comunicación con Claude: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error inesperado al llamar Claude API", e);
            return createErrorResponse("Error interno del servidor");
        }
    }

    /**
     * Pregunta asíncrona a Claude
     */
    public CompletableFuture<ClaudeResponse> askClaudeAsync(String mensaje, String contexto) {
        return CompletableFuture.supplyAsync(() -> askClaude(mensaje, contexto));
    }

    /**
     * Crear payload para la API de Claude
     */
    private ObjectNode createClaudePayload(String mensaje, String contexto) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("max_tokens", maxTokens);
        
        // Crear array de mensajes
        ArrayNode messages = objectMapper.createArrayNode();
        
        // Agregar contexto si existe
        if (contexto != null && !contexto.trim().isEmpty()) {
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", contexto);
            messages.add(systemMessage);
        }
        
        // Agregar mensaje del usuario
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", mensaje);
        messages.add(userMessage);
        
        payload.set("messages", messages);
        
        return payload;
    }

    /**
     * Parsear respuesta de Claude
     */
    private ClaudeResponse parseClaudeResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            if (root.has("content") && root.get("content").isArray()) {
                ArrayNode content = (ArrayNode) root.get("content");
                if (content.size() > 0 && content.get(0).has("text")) {
                    String text = content.get(0).get("text").asText();
                    
                    // Extraer uso de tokens si está disponible
                    int inputTokens = 0;
                    int outputTokens = 0;
                    
                    if (root.has("usage")) {
                        JsonNode usage = root.get("usage");
                        inputTokens = usage.path("input_tokens").asInt(0);
                        outputTokens = usage.path("output_tokens").asInt(0);
                    }
                    
                    return new ClaudeResponse(text, true, null, inputTokens, outputTokens);
                }
            }
            
            return createErrorResponse("Formato de respuesta inesperado de Claude");
            
        } catch (Exception e) {
            logger.error("Error parseando respuesta de Claude", e);
            return createErrorResponse("Error procesando respuesta de Claude");
        }
    }

    /**
     * Crear respuesta de error
     */
    private ClaudeResponse createErrorResponse(String mensaje) {
        return new ClaudeResponse(null, false, mensaje, 0, 0);
    }

    /**
     * Obtener contexto del sistema para el usuario
     */
    public String getSystemContext(String nombreUsuario, String tipoConsulta) {
        StringBuilder context = new StringBuilder();
        context.append("Eres Claude, un asistente IA integrado en el sistema de gestión Árbol-Rama-Hoja. ");
        context.append("El usuario actual es: ").append(nombreUsuario).append(". ");
        
        switch (tipoConsulta.toLowerCase()) {
            case "help":
                context.append("Ayuda al usuario con preguntas sobre cómo usar el sistema, ");
                context.append("explicar funcionalidades, o resolver problemas técnicos. ");
                break;
            case "analysis":
                context.append("Ayuda al usuario a analizar sus datos del sistema, ");
                context.append("generar insights, o interpretar resultados. ");
                break;
            case "general":
            default:
                context.append("Responde de manera útil y profesional. ");
                context.append("Si la pregunta es sobre el sistema, ayuda con eso. ");
                context.append("Si es una pregunta general, responde apropiadamente. ");
                break;
        }
        
        context.append("Mantén las respuestas concisas pero informativas. ");
        context.append("Si no sabes algo específico del sistema, admítelo honestamente.");
        
        return context.toString();
    }

    /**
     * Verificar si la API está configurada
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * DTO para respuestas de Claude
     */
    public static class ClaudeResponse {
        private final String content;
        private final boolean success;
        private final String error;
        private final int inputTokens;
        private final int outputTokens;
        private final LocalDateTime timestamp;

        public ClaudeResponse(String content, boolean success, String error, int inputTokens, int outputTokens) {
            this.content = content;
            this.success = success;
            this.error = error;
            this.inputTokens = inputTokens;
            this.outputTokens = outputTokens;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public String getContent() { return content; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public int getInputTokens() { return inputTokens; }
        public int getOutputTokens() { return outputTokens; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getTotalTokens() { return inputTokens + outputTokens; }
    }
}