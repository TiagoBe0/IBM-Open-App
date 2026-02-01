package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuestas_foro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    @JsonBackReference
    private TemaForo tema;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;

    @Column(name = "editado")
    private Boolean editado = false;

    @Column(name = "activo")
    private Boolean activo = true;

    // Relación opcional: respuesta a otra respuesta (threading/anidación)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_padre_id")
    private RespuestaForo respuestaPadre;

    // Método para marcar como editado
    public void marcarComoEditado() {
        this.editado = true;
        this.fechaEdicion = LocalDateTime.now();
    }
}
