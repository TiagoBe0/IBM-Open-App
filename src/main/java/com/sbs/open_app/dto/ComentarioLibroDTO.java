package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioLibroDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioUsername;
    private Integer numeroParrafo;
    private Integer numeroDocumento;
    private Integer numeroSeccion;
    private String contenido;
    private String imagenUrl;
    private Integer likes;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Boolean esPublico;
}
