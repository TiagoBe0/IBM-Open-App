package com.sbs.open_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuración para manejo de errores específicos de IA Local
 */
@Configuration
public class AIErrorHandlingConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AIErrorHandlingConfig.class);
    
    /**
     * Bean principal para manejo de errores de IA
     */
    @Bean
    public AIErrorHandler aiErrorHandler() {
        logger.info("Inicializando AIErrorHandler para manejo de errores de IA local");
        return new AIErrorHandler();
    }
    
    /**
     * Manejador especializado de errores para servicios de IA
     * Proporciona mensajes de error amigables y determina si los errores son recuperables
     */
    public static class AIErrorHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(AIErrorHandler.class);
        
        // Patrones para identificar tipos de errores
        private static final Pattern CONNECTION_REFUSED_PATTERN = 
            Pattern.compile("connection refused|refused|connection.*reset", Pattern.CASE_INSENSITIVE);
        
        private static final Pattern TIMEOUT_PATTERN = 
            Pattern.compile("timeout|timed out|read timeout", Pattern.CASE_INSENSITIVE);
        
        private static final Pattern MODEL_NOT_FOUND_PATTERN = 
            Pattern.compile("model.*not found|model.*does not exist|model.*missing", Pattern.CASE_INSENSITIVE);
        
        private static final Pattern MEMORY_PATTERN = 
            Pattern.compile("out of memory|memory|insufficient.*memory|oom", Pattern.CASE_INSENSITIVE);
        
        private static final Pattern SERVER_ERROR_PATTERN = 
            Pattern.compile("internal server error|server error|500", Pattern.CASE_INSENSITIVE);
        
        /**
         * Manejar errores específicos de Ollama con mensajes descriptivos
         */
        public String handleOllamaError(Exception e) {
            if (e == null) {
                return "Error desconocido en el servicio de IA";
            }
            
            String message = e.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";
            
            logger.error("Manejando error de Ollama: {}", e.getClass().getSimpleName(), e);
            
            // Errores de conexión
            if (e instanceof ConnectException || CONNECTION_REFUSED_PATTERN.matcher(lowerMessage).find()) {
                return "🔌 Servidor Ollama no está disponible. " +
                       "Verifica que esté ejecutándose con: 'ollama serve'";
            }
            
            // Errores de timeout
            if (e instanceof SocketTimeoutException || TIMEOUT_PATTERN.matcher(lowerMessage).find()) {
                return "⏱️ Timeout en la respuesta de IA. " +
                       "La consulta puede ser muy compleja. Intenta con una pregunta más simple.";
            }
            
            // Modelo no encontrado
            if (MODEL_NOT_FOUND_PATTERN.matcher(lowerMessage).find()) {
                return "🔍 Modelo de IA no encontrado. " +
                       "Verifica que esté descargado con: 'ollama pull [modelo]'";
            }
            
            // Problemas de memoria
            if (MEMORY_PATTERN.matcher(lowerMessage).find()) {
                return "💾 Memoria insuficiente en el servidor de IA. " +
                       "Intenta con un modelo más pequeño o libera memoria.";
            }
            
            // Errores del servidor
            if (SERVER_ERROR_PATTERN.matcher(lowerMessage).find()) {
                return "🚨 Error interno del servidor Ollama. " +
                       "Revisa los logs del servidor para más detalles.";
            }
            
            // Errores de red genéricos
            if (e instanceof ResourceAccessException) {
                return "🌐 Error de comunicación con el servidor de IA. " +
                       "Verifica la conexión de red y que Ollama esté accesible.";
            }
            
            // Error REST genérico
            if (e instanceof RestClientException) {
                return "📡 Error en la comunicación REST con Ollama: " + 
                       (message != null ? message : "Error desconocido");
            }
            
            // Error genérico con mensaje personalizado
            if (message != null && message.length() > 0) {
                return "⚠️ Error inesperado en el servicio de IA: " + message;
            }
            
            return "❌ Error desconocido en el servicio de IA local";
        }
        
        /**
         * Determinar si un error es recuperable (reintentable)
         */
        public boolean isRetryableError(Exception e) {
            if (e == null) return false;
            
            String message = e.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";
            
            // Errores que permiten reintentos
            boolean isRetryable = e instanceof SocketTimeoutException ||
                                TIMEOUT_PATTERN.matcher(lowerMessage).find() ||
                                lowerMessage.contains("connection reset") ||
                                lowerMessage.contains("temporarily unavailable") ||
                                lowerMessage.contains("service unavailable") ||
                                lowerMessage.contains("502") ||
                                lowerMessage.contains("503") ||
                                lowerMessage.contains("504");
            
            logger.debug("Error {} es recuperable: {}", e.getClass().getSimpleName(), isRetryable);
            return isRetryable;
        }
        
        /**
         * Determinar si un error es crítico (requiere intervención manual)
         */
        public boolean isCriticalError(Exception e) {
            if (e == null) return false;
            
            String message = e.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";
            
            // Errores críticos que requieren intervención
            boolean isCritical = e instanceof ConnectException ||
                               CONNECTION_REFUSED_PATTERN.matcher(lowerMessage).find() ||
                               MODEL_NOT_FOUND_PATTERN.matcher(lowerMessage).find() ||
                               lowerMessage.contains("authentication") ||
                               lowerMessage.contains("unauthorized") ||
                               lowerMessage.contains("forbidden");
            
            logger.debug("Error {} es crítico: {}", e.getClass().getSimpleName(), isCritical);
            return isCritical;
        }
        
        /**
         * Obtener código de error HTTP apropiado
         */
        public HttpStatus getHttpStatusForError(Exception e) {
            if (e == null) return HttpStatus.INTERNAL_SERVER_ERROR;
            
            String message = e.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";
            
            // Mapear excepciones a códigos HTTP
            if (e instanceof ConnectException || CONNECTION_REFUSED_PATTERN.matcher(lowerMessage).find()) {
                return HttpStatus.SERVICE_UNAVAILABLE; // 503
            }
            
            if (e instanceof SocketTimeoutException || TIMEOUT_PATTERN.matcher(lowerMessage).find()) {
                return HttpStatus.GATEWAY_TIMEOUT; // 504
            }
            
            if (MODEL_NOT_FOUND_PATTERN.matcher(lowerMessage).find()) {
                return HttpStatus.NOT_FOUND; // 404
            }
            
            if (MEMORY_PATTERN.matcher(lowerMessage).find()) {
                return HttpStatus.INSUFFICIENT_STORAGE; // 507
            }
            
            // Error genérico
            return HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }
        
        /**
         * Crear respuesta de error estandarizada
         */
        public Map<String, Object> createErrorResponse(Exception e, String userMessage) {
            Map<String, Object> errorResponse = new HashMap<>();
            
            errorResponse.put("success", false);
            errorResponse.put("error", userMessage != null ? userMessage : handleOllamaError(e));
            errorResponse.put("timestamp", System.currentTimeMillis());
            errorResponse.put("retryable", isRetryableError(e));
            errorResponse.put("critical", isCriticalError(e));
            
            // Información adicional para debugging (solo en desarrollo)
            if (logger.isDebugEnabled()) {
                errorResponse.put("exception_type", e.getClass().getSimpleName());
                errorResponse.put("exception_message", e.getMessage());
            }
            
            return errorResponse;
        }
        
        /**
         * Registrar error con nivel apropiado
         */
        public void logError(Exception e, String context) {
            if (isCriticalError(e)) {
                logger.error("Error crítico en {}: {}", context, e.getMessage(), e);
            } else if (isRetryableError(e)) {
                logger.warn("Error recuperable en {}: {}", context, e.getMessage());
            } else {
                logger.error("Error en {}: {}", context, e.getMessage(), e);
            }
        }
        
        /**
         * Obtener sugerencias de solución para errores
         */
        public String getSuggestion(Exception e) {
            String message = e.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";
            
            if (e instanceof ConnectException || CONNECTION_REFUSED_PATTERN.matcher(lowerMessage).find()) {
                return "Ejecuta 'ollama serve' para iniciar el servidor, " +
                       "o verifica que esté ejecutándose en el puerto correcto.";
            }
            
            if (TIMEOUT_PATTERN.matcher(lowerMessage).find()) {
                return "Intenta con una consulta más simple, " +
                       "aumenta el timeout en la configuración, " +
                       "o usa un modelo más pequeño.";
            }
            
            if (MODEL_NOT_FOUND_PATTERN.matcher(lowerMessage).find()) {
                return "Descarga el modelo con 'ollama pull [nombre-modelo]' " +
                       "o verifica el nombre del modelo en la configuración.";
            }
            
            if (MEMORY_PATTERN.matcher(lowerMessage).find()) {
                return "Libera memoria del sistema, " +
                       "usa un modelo más pequeño, " +
                       "o aumenta la memoria asignada a Ollama.";
            }
            
            return "Revisa los logs de Ollama y verifica la configuración del sistema.";
        }
    }
}

/**
 * Controlador de advice global para manejo centralizado de errores de IA
 */
@ControllerAdvice
class GlobalAIErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalAIErrorHandler.class);
    
    /**
     * Manejar errores específicos de ResourceAccessException (problemas de red/conexión)
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException e) {
        logger.error("Error de acceso a recurso en IA: ", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "No se puede conectar con el servidor de IA local");
        errorResponse.put("suggestion", "Verifica que Ollama esté ejecutándose");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * Manejar errores REST genéricos en endpoints de IA
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException e) {
        logger.error("Error de cliente REST en IA: ", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Error de comunicación con el servicio de IA");
        errorResponse.put("details", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
    
    /**
     * Manejar errores de timeout específicamente
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleSocketTimeoutException(SocketTimeoutException e) {
        logger.warn("Timeout en solicitud de IA: ", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Timeout en la respuesta de IA");
        errorResponse.put("suggestion", "Intenta con una consulta más simple");
        errorResponse.put("retryable", true);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }
}