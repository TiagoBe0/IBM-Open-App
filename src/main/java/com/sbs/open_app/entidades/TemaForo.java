package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temas_foro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemaForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonBackReference
    private CategoriaForo categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_ultima_actividad")
    private LocalDateTime fechaUltimaActividad = LocalDateTime.now();

    @Column(name = "vistas")
    private Integer vistas = 0;

    @Column(name = "fijado")
    private Boolean fijado = false;  // Temas importantes que aparecen primero

    @Column(name = "cerrado")
    private Boolean cerrado = false;  // No se pueden agregar más respuestas

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("fechaCreacion ASC")
    private List<RespuestaForo> respuestas = new ArrayList<>();

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaSubida ASC")
    private List<ArchivoForo> archivos = new ArrayList<>();

    // Método para obtener cantidad de respuestas
    public int getCantidadRespuestas() {
        return respuestas != null ? respuestas.size() : 0;
    }

    // Método para obtener cantidad de archivos
    public int getCantidadArchivos() {
        return archivos != null ? archivos.size() : 0;
    }

    // Método para verificar si tiene archivos adjuntos
    public boolean tieneArchivos() {
        return archivos != null && !archivos.isEmpty();
    }

    // Método para incrementar vistas
    public void incrementarVistas() {
        this.vistas = (this.vistas == null ? 0 : this.vistas) + 1;
    }

    // Método para actualizar última actividad
    public void actualizarUltimaActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }

    // Método para obtener la última respuesta
    public RespuestaForo getUltimaRespuesta() {
        if (respuestas == null || respuestas.isEmpty()) {
            return null;
        }
        return respuestas.get(respuestas.size() - 1);
    }
}
