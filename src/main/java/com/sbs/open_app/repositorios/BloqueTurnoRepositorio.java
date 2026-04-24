package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.BloqueTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface BloqueTurnoRepositorio extends JpaRepository<BloqueTurno, Long> {

    List<BloqueTurno> findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();

    List<BloqueTurno> findByDiaSemanaAndActivoTrue(DayOfWeek diaSemana);
}
