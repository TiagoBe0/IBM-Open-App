package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un "like" (vela) en un comentario del libro
 */
@Entity
@Table(name = "likes_comentarios",
       uniqueConstraints = @UniqueConstraint(columnNames = {"comentario_id", "usuario_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comentario_id", nullable = false)
    private ComentarioLibro comentario;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
