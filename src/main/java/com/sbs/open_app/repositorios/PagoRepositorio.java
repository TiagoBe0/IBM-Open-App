package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {

    Optional<Pago> findByTurnoId(Long turnoId);
}
