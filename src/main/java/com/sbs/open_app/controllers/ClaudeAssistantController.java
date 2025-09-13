package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.ChatMessage;
import com.sbs.open_app.servicios.ClaudeService;
import com.sbs.open_app.servicios.ClaudeService.ClaudeResponse;
import com.sbs.open_app.servicios.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class ClaudeAssistantController {

    @Autowired
    private ClaudeService claudeService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Procesar pregunta a Claude vía WebSocket (para chat en tiempo real)
     */
    @MessageMapping("/chat.askClaude")
    public void askClaudeViaSocket(@Payload Map<String, Object> request) {
        String mensaje = (String) request.get("mensaje");
        String usuarioNombre = (String) request.get("usuarioNombre");
        Long usuarioId = Long.parseLong(request.get("usuarioId").toString());
        String tipoConsulta = (String) request.getOrDefault("tipo", "general");

        // Procesar asincrónicamente para no bloquear el WebSocket
        CompletableFuture.supplyAsync(() -> {
            // Obtener contexto del sistema
            String contexto = claudeService.getSystemContext(usuarioNombre, tipoConsulta);
            
            // Hacer pregunta a Claude
            return claudeService.askClaude(mensaje, contexto);
        }).thenAccept(response -> {
            // Crear mensaje de respuesta
            ChatMessage claudeMessage = new ChatMessage();
            claudeMessage.setUsuarioId(0L); // ID especial para Claude
            claudeMessage.setUsuarioNombre("🤖 Claude Assistant");
            claudeMessage.setSalaChat("general");
            claudeMessage.setTipo(ChatMessage.TipoMensaje.CHAT);

            if (response.isSuccess()) {
                claudeMessage.setContenido(response.getContent());
            } else {
                claudeMessage.setContenido("❌ " + response.getError());
                claudeMessage.setTipo(ChatMessage.TipoMensaje.SYSTEM);
            }

            // Guardar en base de datos
            try {
                ChatMessage savedMessage = chatService.guardarMensaje(claudeMessage);
                
                // Enviar respuesta a todos los usuarios conectados
                messagingTemplate.convertAndSend("/topic/public", savedMessage);
                
                // Si hay información de tokens, enviar estadísticas
                if (response.isSuccess() && response.getTotalTokens() > 0) {
                    Map<String, Object> stats = Map.of(
                        "type", "claude_stats",
                        "inputTokens", response.getInputTokens(),
                        "outputTokens", response.getOutputTokens(),
                        "totalTokens", response.getTotalTokens()
                    );
                    messagingTemplate.convertAndSend("/topic/public", stats);
                }
                
            } catch (Exception e) {
                // Enviar mensaje de error si algo falla
                ChatMessage errorMessage = new ChatMessage();
                errorMessage.setUsuarioId(0L);
                errorMessage.setUsuarioNombre("Sistema");
                errorMessage.setContenido("Error procesando respuesta de Claude");
                errorMessage.setTipo(ChatMessage.TipoMensaje.SYSTEM);
                errorMessage.setSalaChat("general");
                
                messagingTemplate.convertAndSend("/topic/public", errorMessage);
            }
        }).exceptionally(throwable -> {
            // Manejar errores de la operación asíncrona
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setUsuarioId(0L);
            errorMessage.setUsuarioNombre("Sistema");
            errorMessage.setContenido("Error interno al procesar consulta a Claude");
            errorMessage.setTipo(ChatMessage.TipoMensaje.SYSTEM);
            errorMessage.setSalaChat("general");
            
            messagingTemplate.convertAndSend("/topic/public", errorMessage);
            return null;
        });
    }
}

/**
 * REST Controller para Claude Assistant
 */
@RestController
@RequestMapping("/api/claude")
@CrossOrigin(origins = "*")
class ClaudeAssistantRestController {

    @Autowired
    private ClaudeService claudeService;

    /**
     * Endpoint REST para preguntas directas a Claude
     */
    @PostMapping("/ask")
    public ResponseEntity<ClaudeResponse> askClaude(@RequestBody ClaudeRequest request) {
        if (!claudeService.isConfigured()) {
            ClaudeResponse errorResponse = new ClaudeResponse(
                null, false, "Claude API no está configurada", 0, 0
            );
            return ResponseEntity.ok(errorResponse);
        }

        String contexto = claudeService.getSystemContext(
            request.getUsuarioNombre(), 
            request.getTipo()
        );
        
        ClaudeResponse response = claudeService.askClaude(request.getMensaje(), contexto);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint asíncrono para Claude
     */
    @PostMapping("/ask-async")
    public CompletableFuture<ResponseEntity<ClaudeResponse>> askClaudeAsync(@RequestBody ClaudeRequest request) {
        if (!claudeService.isConfigured()) {
            ClaudeResponse errorResponse = new ClaudeResponse(
                null, false, "Claude API no está configurada", 0, 0
            );
            return CompletableFuture.completedFuture(ResponseEntity.ok(errorResponse));
        }

        String contexto = claudeService.getSystemContext(
            request.getUsuarioNombre(), 
            request.getTipo()
        );
        
        return claudeService.askClaudeAsync(request.getMensaje(), contexto)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Verificar estado de la configuración de Claude
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean configured = claudeService.isConfigured();
        
        Map<String, Object> status = Map.of(
            "configured", configured,
            "message", configured ? "Claude API está configurada y lista" : "Claude API no está configurada",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(status);
    }

    /**
     * Obtener sugerencias de comandos para Claude
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        Map<String, Object> suggestions = Map.of(
            "help", Map.of(
                "title", "Ayuda del Sistema",
                "examples", new String[]{
                    "¿Cómo crear un nuevo árbol?",
                    "Explícame la diferencia entre ramas y hojas",
                    "¿Cómo puedo editar mis datos?"
                }
            ),
            "analysis", Map.of(
                "title", "Análisis de Datos",
                "examples", new String[]{
                    "Analiza mis datos actuales",
                    "¿Qué patrones ves en mi información?",
                    "Sugiere mejoras para mi estructura de datos"
                }
            ),
            "general", Map.of(
                "title", "Consultas Generales",
                "examples", new String[]{
                    "¿Qué es la inteligencia artificial?",
                    "Explícame Spring Boot",
                    "Dame consejos de programación"
                }
            )
        );
        
        return ResponseEntity.ok(suggestions);
    }

    /**
     * DTO para requests a Claude
     */
    public static class ClaudeRequest {
        private String mensaje;
        private String usuarioNombre;
        private String tipo = "general";

        public ClaudeRequest() {}

        public ClaudeRequest(String mensaje, String usuarioNombre, String tipo) {
            this.mensaje = mensaje;
            this.usuarioNombre = usuarioNombre;
            this.tipo = tipo;
        }

        // Getters y Setters
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        
        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
        
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }
}