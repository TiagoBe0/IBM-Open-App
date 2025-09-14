package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmistadDTO {
    
    private Long id;
    private Long usuarioId;
    private Long amigoId;
    private String estado; // PENDIENTE, ACEPTADA, RECHAZADA, BLOQUEADA
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAceptacion;
    private String mensajeSolicitud;
    
    // Información adicional del amigo
    private String amigoNombre;
    private String amigoApellido;
    private String amigoUsername;
    private String amigoEmail;
    private String amigoFotoPerfil;
    
    // Información adicional del usuario que envía
    private String usuarioNombre;
    private String usuarioApellido;
    private String usuarioUsername;
    // Constructor para respuestas simplificadas
    public AmistadDTO(Long id, String estado, LocalDateTime fechaSolicitud) {
        this.id = id;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }
}

