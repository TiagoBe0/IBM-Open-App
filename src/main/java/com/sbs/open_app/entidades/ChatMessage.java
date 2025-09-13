package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String contenido;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "usuario_nombre", nullable = false)
    private String usuarioNombre;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMensaje tipo = TipoMensaje.CHAT;
    
    @Column(name = "sala_chat")
    private String salaChat = "general"; // Para chats grupales o por salas
    
    // Constructor vacío requerido por JPA
    public ChatMessage() {
        this.fechaEnvio = LocalDateTime.now();
    }
    
    // Constructor completo
    public ChatMessage(String contenido, Long usuarioId, String usuarioNombre) {
        this();
        this.contenido = contenido;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
    }
    
    // Constructor con sala
    public ChatMessage(String contenido, Long usuarioId, String usuarioNombre, String salaChat) {
        this(contenido, usuarioId, usuarioNombre);
        this.salaChat = salaChat;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
    
    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
    
    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
    
    public TipoMensaje getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }
    
    public String getSalaChat() {
        return salaChat;
    }
    
    public void setSalaChat(String salaChat) {
        this.salaChat = salaChat;
    }
    
    // Enum para tipos de mensaje
    public enum TipoMensaje {
        CHAT,      // Mensaje normal
        JOIN,      // Usuario se unió
        LEAVE,     // Usuario se fue
        SYSTEM     // Mensaje del sistema
    }
}