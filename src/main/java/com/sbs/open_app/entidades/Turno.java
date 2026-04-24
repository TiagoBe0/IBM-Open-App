package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "turnos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "hora"}, name = "uk_turno_fecha_hora"))
@Data
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_paciente", nullable = false, length = 100)
    private String nombrePaciente;

    @Column(name = "email_paciente", nullable = false, length = 150)
    private String emailPaciente;

    @Column(name = "telefono_paciente", length = 30)
    private String telefonoPaciente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado = EstadoTurno.RESERVADO;

    @Column(length = 500)
    private String motivo;

    @Column(length = 500)
    private String notas;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @OneToOne(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    @PrePersist
    protected void onCreate() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }

    public enum EstadoTurno {
        RESERVADO, CANCELADO, COMPLETADO, AUSENTE
    }
}
