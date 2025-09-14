package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversacionDTO {
    private Long amigoId;
    private String amigoNombre;
    private String amigoApellido;
    private String amigoEmail;
    private String amigoFotoPerfil;
    private MensajeDTO ultimoMensaje;
    private long mensajesNoLeidos;
    private LocalDateTime ultimaActividad;
}