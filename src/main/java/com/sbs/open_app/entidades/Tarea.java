package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(length = 50)
    private String estado; // "Pendiente", "En progreso", "Completada", "Cancelada"

    @Column(length = 20)
    private String prioridad; // "Alta", "Media", "Baja"

    @Column(name = "completada")
    private boolean completada;

    @Column(name = "asignado_a", length = 100)
    private String asignadoA; // Nombre de la persona asignada (opcional)

    @Column(length = 100)
    private String etiquetas; // Tags separados por comas

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    @JsonBackReference
    private Proyecto proyecto;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "foto_id")
    private Foto foto;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
