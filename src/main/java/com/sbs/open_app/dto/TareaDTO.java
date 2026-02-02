package com.sbs.open_app.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaCreacion;
    private String estado;
    private String prioridad;
    private boolean completada;
    private String asignadoA;
    private String etiquetas;
    private Long proyectoId;
    private Long fotoId;
    private Long documentoId;
}
