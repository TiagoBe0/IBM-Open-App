package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;
    
    @Column(name = "contenido", nullable = false, length = 2000)
    private String contenido;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();
    
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
    
    @Column(name = "leido")
    private Boolean leido = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mensaje")
    private TipoMensaje tipoMensaje = TipoMensaje.TEXTO;
    
    @Column(name = "archivo_adjunto")
    private String archivoAdjunto;
    
    @Column(name = "eliminado_remitente")
    private Boolean eliminadoRemitente = false;
    
    @Column(name = "eliminado_destinatario")
    private Boolean eliminadoDestinatario = false;
    
    // Enum para tipos de mensaje
    public enum TipoMensaje {
        TEXTO,
        IMAGEN,
        ARCHIVO,
        SISTEMA
    }
    
    // Métodos de utilidad
    public boolean isLeido() {
        return leido != null && leido;
    }
    
    public void marcarComoLeido() {
        this.leido = true;
        this.fechaLectura = LocalDateTime.now();
    }
    
    public boolean isVisible() {
        return !eliminadoRemitente && !eliminadoDestinatario;
    }
    
    public boolean isVisiblePara(Long usuarioId) {
        if (remitente.getId().equals(usuarioId)) {
            return !eliminadoRemitente;
        } else if (destinatario.getId().equals(usuarioId)) {
            return !eliminadoDestinatario;
        }
        return false;
    }
}