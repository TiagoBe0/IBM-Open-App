package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.Turno;
import com.sbs.open_app.servicios.TurnoServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class CalendarioController {

    private final TurnoServicio turnoServicio;

    public CalendarioController(TurnoServicio turnoServicio) {
        this.turnoServicio = turnoServicio;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/turnos";
    }

    @GetMapping("/turnos")
    public String calendario(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(30);
        Map<LocalDate, List<LocalTime>> disponibilidad = turnoServicio.getSlotsDisponibles(hoy, hasta);
        model.addAttribute("disponibilidad", disponibilidad);
        model.addAttribute("hoy", hoy);
        return "calendario";
    }

    @GetMapping("/api/turnos/disponibilidad")
    @ResponseBody
    public ResponseEntity<Map<LocalDate, List<LocalTime>>> getDisponibilidad(
            @RequestParam(defaultValue = "0") int offset) {
        LocalDate desde = LocalDate.now().plusDays(offset * 30L);
        LocalDate hasta = desde.plusDays(29);
        Map<LocalDate, List<LocalTime>> disponibilidad = turnoServicio.getSlotsDisponibles(desde, hasta);
        return ResponseEntity.ok(disponibilidad);
    }

    @GetMapping("/turnos/reservar")
    public String mostrarFormulario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String hora,
            Model model) {
        LocalTime horaLocal = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", horaLocal);
        model.addAttribute("fechaFormateada", fecha.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM yyyy",
                new java.util.Locale("es", "AR"))));
        return "reservar";
    }

    @PostMapping("/turnos/reservar")
    public String procesarReserva(
            @RequestParam String nombrePaciente,
            @RequestParam String emailPaciente,
            @RequestParam(required = false) String telefonoPaciente,
            @RequestParam(required = false) String motivo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String hora,
            RedirectAttributes redirectAttributes) {

        try {
            LocalTime horaLocal = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            Turno turno = turnoServicio.reservarTurno(
                    nombrePaciente.trim(), emailPaciente.trim().toLowerCase(),
                    telefonoPaciente, motivo, fecha, horaLocal);
            redirectAttributes.addFlashAttribute("turno", turno);
            return "redirect:/turnos/confirmacion/" + turno.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/turnos";
        }
    }

    @GetMapping("/turnos/confirmacion/{id}")
    public String confirmacion(@PathVariable Long id, Model model) {
        turnoServicio.getTurnoPorId(id).ifPresent(t -> model.addAttribute("turno", t));
        return "confirmacion";
    }

    @GetMapping("/turnos/mis-turnos")
    public String misTurnos(@RequestParam(required = false) String email, Model model) {
        if (email != null && !email.isBlank()) {
            List<Turno> turnos = turnoServicio.getTurnosPorEmail(email.trim().toLowerCase());
            model.addAttribute("turnos", turnos);
            model.addAttribute("email", email);
        }
        return "mis-turnos";
    }

    @PostMapping("/turnos/cancelar/{id}")
    public String cancelarTurno(@PathVariable Long id, @RequestParam String email, RedirectAttributes ra) {
        turnoServicio.getTurnoPorId(id).ifPresent(t -> {
            if (t.getEmailPaciente().equalsIgnoreCase(email.trim())) {
                turnoServicio.cambiarEstado(id, Turno.EstadoTurno.CANCELADO);
            }
        });
        ra.addAttribute("email", email);
        return "redirect:/turnos/mis-turnos";
    }
}
