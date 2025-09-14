package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {
    
    private Long id;
    private Long remitenteId;
    private Long destinatarioId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLectura;
    private Boolean leido;
    private String tipoMensaje;
    private String archivoAdjunto;
    
    // Información adicional del remitente
    private String remitenteNombre;
    private String remitenteApellido;
    private String remitenteEmail;
    private String remitenteFotoPerfil;
    
    // Información adicional del destinatario
    private String destinatarioNombre;
    private String destinatarioApellido;
    private String destinatarioEmail;
    private String destinatarioFotoPerfil;
    
    // Metadatos
    private boolean esPropio; // true si el usuario actual es el remitente
    private long minutosTranscurridos;
}