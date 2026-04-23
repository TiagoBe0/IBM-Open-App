package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.BloqueTurno;
import com.sbs.open_app.entidades.Turno;
import com.sbs.open_app.repositorios.BloqueTurnoRepositorio;
import com.sbs.open_app.repositorios.TurnoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class TurnoServicio {

    private final TurnoRepositorio turnoRepositorio;
    private final BloqueTurnoRepositorio bloqueRepositorio;

    public TurnoServicio(TurnoRepositorio turnoRepositorio, BloqueTurnoRepositorio bloqueRepositorio) {
        this.turnoRepositorio = turnoRepositorio;
        this.bloqueRepositorio = bloqueRepositorio;
    }

    /**
     * Genera los slots disponibles para un rango de fechas dado, excluyendo los ya reservados.
     * Retorna un mapa de fecha -> lista de horas disponibles.
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, List<LocalTime>> getSlotsDisponibles(LocalDate desde, LocalDate hasta) {
        List<BloqueTurno> bloques = bloqueRepositorio.findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();

        // Turnos ya tomados en el rango
        Set<String> tomados = new HashSet<>();
        List<Object[]> slotsTomados = turnoRepositorio.findSlotsTomados(desde, hasta);
        for (Object[] row : slotsTomados) {
            tomados.add(row[0].toString() + "|" + row[1].toString());
        }

        Map<LocalDate, List<LocalTime>> resultado = new LinkedHashMap<>();
        LocalDate cursor = desde;

        while (!cursor.isAfter(hasta)) {
            final LocalDate fecha = cursor;
            List<LocalTime> horasDelDia = new ArrayList<>();

            for (BloqueTurno bloque : bloques) {
                if (bloque.getDiaSemana() == fecha.getDayOfWeek()) {
                    LocalTime slot = bloque.getHoraInicio();
                    while (!slot.isAfter(bloque.getHoraFin().minusMinutes(bloque.getDuracionMinutos()))) {
                        String key = fecha + "|" + slot;
                        if (!tomados.contains(key)) {
                            horasDelDia.add(slot);
                        }
                        slot = slot.plusMinutes(bloque.getDuracionMinutos());
                    }
                }
            }

            if (!horasDelDia.isEmpty()) {
                resultado.put(fecha, horasDelDia);
            }

            cursor = cursor.plusDays(1);
        }

        return resultado;
    }

    /**
     * Reserva un turno. Lanza excepción si el slot ya fue tomado (concurrencia).
     */
    public Turno reservarTurno(String nombrePaciente, String emailPaciente, String telefonoPaciente,
                               String motivo, LocalDate fecha, LocalTime hora) {

        if (turnoRepositorio.existsByFechaAndHoraAndEstadoNot(fecha, hora, Turno.EstadoTurno.CANCELADO)) {
            throw new IllegalStateException("El horario seleccionado ya no está disponible. Por favor, elegí otro.");
        }

        Turno turno = new Turno();
        turno.setNombrePaciente(nombrePaciente);
        turno.setEmailPaciente(emailPaciente);
        turno.setTelefonoPaciente(telefonoPaciente);
        turno.setMotivo(motivo);
        turno.setFecha(fecha);
        turno.setHora(hora);
        turno.setEstado(Turno.EstadoTurno.RESERVADO);

        return turnoRepositorio.save(turno);
    }

    @Transactional(readOnly = true)
    public List<Turno> getTurnosPorEmail(String email) {
        return turnoRepositorio.findByEmailPacienteOrderByFechaDescHoraDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Turno> getProximosTurnos() {
        return turnoRepositorio.findProximosTurnos(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Turno> getTurnosPorFecha(LocalDate fecha) {
        return turnoRepositorio.findByFechaOrderByHoraAsc(fecha);
    }

    @Transactional(readOnly = true)
    public Optional<Turno> getTurnoPorId(Long id) {
        return turnoRepositorio.findById(id);
    }

    public Turno cambiarEstado(Long id, Turno.EstadoTurno nuevoEstado) {
        Turno turno = turnoRepositorio.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Turno no encontrado"));
        turno.setEstado(nuevoEstado);
        return turnoRepositorio.save(turno);
    }

    public void eliminarTurno(Long id) {
        turnoRepositorio.deleteById(id);
    }

    // --- Bloques de disponibilidad ---

    @Transactional(readOnly = true)
    public List<BloqueTurno> getBloques() {
        return bloqueRepositorio.findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();
    }

    @Transactional(readOnly = true)
    public List<BloqueTurno> getTodosLosBloques() {
        return bloqueRepositorio.findAll();
    }

    public BloqueTurno guardarBloque(BloqueTurno bloque) {
        return bloqueRepositorio.save(bloque);
    }

    public void eliminarBloque(Long id) {
        bloqueRepositorio.deleteById(id);
    }
}
