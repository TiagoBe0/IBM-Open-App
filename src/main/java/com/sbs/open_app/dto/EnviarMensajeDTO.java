package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnviarMensajeDTO {
    private Long destinatarioId;
    private String contenido;
    private String tipoMensaje = "TEXTO";
    private String archivoAdjunto;
}