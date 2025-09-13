// Foto.java - Entidad
package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "foto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Foto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "mime", length = 100)
    private String mime;
    
    @Lob
    @Column(name = "contenido")
    private byte[] contenido;
}