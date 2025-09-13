package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.ChatMessage;
import com.sbs.open_app.repositorios.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.sbs.open_app.dto.ChatStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Guardar un mensaje en la base de datos
     */
    public ChatMessage guardarMensaje(ChatMessage mensaje) {
        // Validaciones básicas
        if (mensaje.getContenido() == null || mensaje.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío");
        }
        
        if (mensaje.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario es requerido");
        }
        
        // Limpiar y preparar el mensaje
        mensaje.setContenido(mensaje.getContenido().trim());
        
        if (mensaje.getFechaEnvio() == null) {
            mensaje.setFechaEnvio(LocalDateTime.now());
        }
        
        if (mensaje.getSalaChat() == null) {
            mensaje.setSalaChat("general");
        }
        
        if (mensaje.getTipo() == null) {
            mensaje.setTipo(ChatMessage.TipoMensaje.CHAT);
        }

        return chatMessageRepository.save(mensaje);
    }

    /**
     * Obtener los últimos mensajes de una sala
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> obtenerUltimosMensajes(String salaChat, int limite) {
        List<ChatMessage> mensajes = chatMessageRepository.findUltimosMensajes(salaChat, limite);
        
        // Invertir para mostrar cronológicamente (más antiguos primero)
        return mensajes.stream()
                .sorted((m1, m2) -> m1.getFechaEnvio().compareTo(m2.getFechaEnvio()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los mensajes de una sala
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> obtenerTodosMensajes(String salaChat) {
        return chatMessageRepository.findBySalaChatOrderByFechaEnvioDesc(salaChat);
    }

    /**
     * Obtener mensajes desde una fecha específica
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> obtenerMensajesDesde(String salaChat, LocalDateTime fechaDesde) {
        return chatMessageRepository.findMensajesDesde(salaChat, fechaDesde);
    }

    /**
     * Obtener mensajes de un usuario específico
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> obtenerMensajesUsuario(Long usuarioId) {
        return chatMessageRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuarioId);
    }

    /**
     * Buscar mensajes por contenido
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> buscarMensajes(String salaChat, String texto) {
        return chatMessageRepository.buscarMensajesPorTexto(salaChat, texto);
    }

    /**
     * Eliminar un mensaje (solo el propietario o admin)
     */
    public void eliminarMensaje(Long mensajeId, Long usuarioId) {
        ChatMessage mensaje = chatMessageRepository.findById(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        
        // Verificar que el usuario es el propietario del mensaje
        if (!mensaje.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar este mensaje");
        }
        
        chatMessageRepository.deleteById(mensajeId);
    }

    /**
     * Obtener estadísticas del chat
     */
    @Transactional(readOnly = true)
    public ChatStats obtenerEstadisticas(String salaChat) {
        Long totalMensajes = chatMessageRepository.contarMensajesPorSala(salaChat);
        
        // Contar usuarios únicos que han enviado mensajes
        List<ChatMessage> mensajes = chatMessageRepository.findBySalaChatOrderByFechaEnvioDesc(salaChat);
        Long usuariosActivos = mensajes.stream()
                .map(ChatMessage::getUsuarioId)
                .distinct()
                .count();
        
        return new ChatStats(totalMensajes, usuariosActivos);
    }

    /**
     * Limpiar mensajes antiguos (para mantenimiento)
     */
    @Transactional
    public void limpiarMensajesAntiguos(String salaChat, LocalDateTime fechaLimite) {
        List<ChatMessage> mensajesAntiguos = chatMessageRepository.findBySalaChatOrderByFechaEnvioDesc(salaChat)
                .stream()
                .filter(m -> m.getFechaEnvio().isBefore(fechaLimite))
                .collect(Collectors.toList());
        
        chatMessageRepository.deleteAll(mensajesAntiguos);
    }

    /**
     * Crear mensaje del sistema
     */
    public ChatMessage crearMensajeSistema(String contenido, String salaChat) {
        ChatMessage mensaje = new ChatMessage();
        mensaje.setContenido(contenido);
        mensaje.setUsuarioId(0L); // ID especial para mensajes del sistema
        mensaje.setUsuarioNombre("Sistema");
        mensaje.setSalaChat(salaChat);
        mensaje.setTipo(ChatMessage.TipoMensaje.SYSTEM);
        mensaje.setFechaEnvio(LocalDateTime.now());
        
        return guardarMensaje(mensaje);
    }
}