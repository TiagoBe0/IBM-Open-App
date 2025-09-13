package com.sbs.open_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria para enviar mensajes a clientes suscritos
        config.enableSimpleBroker("/topic", "/queue");
        
        // Define el prefijo para mensajes destinados a métodos anotados con @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Opcional: Prefijo para mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint WebSocket que los clientes usarán para conectarse
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // En producción, especifica dominios exactos
                .withSockJS(); // Habilita SockJS como fallback para navegadores que no soportan WebSocket
        
        // Endpoint adicional sin SockJS para clientes que soportan WebSocket nativo
        registry.addEndpoint("/ws-chat-native")
                .setAllowedOriginPatterns("*");
    }
}