package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_foro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "icono", length = 50)
    private String icono;  // Emoji o clase de icono

    @Column(name = "color", length = 20)
    private String color;  // Color hex para la categoría

    @Column(name = "orden")
    private Integer orden = 0;  // Para ordenar las categorías

    @Column(name = "activa")
    private Boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TemaForo> temas = new ArrayList<>();

    // Método para obtener cantidad de temas
    public int getCantidadTemas() {
        return temas != null ? temas.size() : 0;
    }

    // Método para obtener cantidad de respuestas en todos los temas
    public int getCantidadRespuestas() {
        if (temas == null) return 0;
        return temas.stream()
                .mapToInt(TemaForo::getCantidadRespuestas)
                .sum();
    }
}
