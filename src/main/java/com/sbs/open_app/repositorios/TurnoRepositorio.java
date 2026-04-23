package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TurnoRepositorio extends JpaRepository<Turno, Long> {

    List<Turno> findByFechaBetweenOrderByFechaAscHoraAsc(LocalDate inicio, LocalDate fin);

    List<Turno> findByFechaOrderByHoraAsc(LocalDate fecha);

    List<Turno> findByEmailPacienteOrderByFechaDescHoraDesc(String email);

    Optional<Turno> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, Turno.EstadoTurno estado);

    @Query("SELECT t FROM Turno t WHERE t.fecha >= :desde ORDER BY t.fecha ASC, t.hora ASC")
    List<Turno> findProximosTurnos(@Param("desde") LocalDate desde);

    @Query("SELECT t.fecha, t.hora FROM Turno t WHERE t.fecha BETWEEN :inicio AND :fin AND t.estado = 'RESERVADO'")
    List<Object[]> findSlotsTomados(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
