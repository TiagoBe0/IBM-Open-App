package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "amistades", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "amigo_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amistad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Quien envía la solicitud
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amigo_id", nullable = false)
    private Usuario amigo; // Quien recibe la solicitud
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAmistad estado = EstadoAmistad.PENDIENTE;
    
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
    
    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;
    
    @Column(name = "mensaje_solicitud", length = 500)
    private String mensajeSolicitud;
    
    // Enum para estados de amistad
    public enum EstadoAmistad {
        PENDIENTE,    // Solicitud enviada pero no respondida
        ACEPTADA,     // Amistad confirmada
        RECHAZADA,    // Solicitud rechazada
        BLOQUEADA     // Usuario bloqueado
    }
    
    // Método helper para verificar si la amistad está activa
    public boolean isActiva() {
        return estado == EstadoAmistad.ACEPTADA;
    }
    
    // Método helper para verificar si está pendiente
    public boolean isPendiente() {
        return estado == EstadoAmistad.PENDIENTE;
    }
}