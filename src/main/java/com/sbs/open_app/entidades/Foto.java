package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "foto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String mime;   // ej: image/jpeg
    
    @Column(length = 255)
    private String nombre; // nombre original
    
    @Lob
    // QUITAR LA LÍNEA columnDefinition COMPLETAMENTE para PostgreSQL
    private byte[] contenido;
}