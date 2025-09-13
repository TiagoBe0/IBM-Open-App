package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.ChatMessage;
import com.sbs.open_app.servicios.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Manejar envío de mensajes de chat
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // Guardar el mensaje en la base de datos
        return chatService.guardarMensaje(chatMessage);
    }

    /**
     * Manejar cuando un usuario se une al chat
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        // Crear mensaje de sistema
        ChatMessage mensajeSistema = new ChatMessage(
            chatMessage.getUsuarioNombre() + " se unió al chat",
            chatMessage.getUsuarioId(),
            "Sistema"
        );
        mensajeSistema.setTipo(ChatMessage.TipoMensaje.JOIN);
        
        // Guardar mensaje de sistema
        return chatService.guardarMensaje(mensajeSistema);
    }
}

/**
 * REST Controller para operaciones HTTP del chat
 */
@RestController
@RequestMapping("/api/chat")
class ChatRestController {

    @Autowired
    private ChatService chatService;

    /**
     * Obtener historial de mensajes de una sala
     */
    @GetMapping("/messages/{salaChat}")
    public List<ChatMessage> obtenerMensajes(@PathVariable String salaChat,
                                           @RequestParam(defaultValue = "50") int limite) {
        return chatService.obtenerUltimosMensajes(salaChat, limite);
    }

    /**
     * Obtener mensajes de un usuario específico
     */
    @GetMapping("/messages/user/{usuarioId}")
    public List<ChatMessage> obtenerMensajesUsuario(@PathVariable Long usuarioId) {
        return chatService.obtenerMensajesUsuario(usuarioId);
    }
}