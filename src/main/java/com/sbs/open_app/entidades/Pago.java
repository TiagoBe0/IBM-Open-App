package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodo = MetodoPago.CONSULTORIO;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "referencia_externa", length = 200)
    private String referenciaExterna;

    public enum EstadoPago {
        PENDIENTE, COMPLETADO, REEMBOLSADO, FALLIDO
    }

    public enum MetodoPago {
        CONSULTORIO, MERCADO_PAGO, TRANSFERENCIA
    }
}
