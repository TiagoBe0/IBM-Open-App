package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.BloqueTurno;
import com.sbs.open_app.entidades.Turno;
import com.sbs.open_app.servicios.TurnoServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminTurnosController {

    private final TurnoServicio turnoServicio;

    public AdminTurnosController(TurnoServicio turnoServicio) {
        this.turnoServicio = turnoServicio;
    }

    @GetMapping({"", "/", "/panel"})
    public String panel(Model model) {
        List<Turno> proximos = turnoServicio.getProximosTurnos();
        model.addAttribute("turnos", proximos);
        model.addAttribute("hoy", LocalDate.now());
        return "admin/panel";
    }

    @GetMapping("/turnos/{fecha}")
    public String turnosPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {
        model.addAttribute("turnos", turnoServicio.getTurnosPorFecha(fecha));
        model.addAttribute("fecha", fecha);
        return "admin/turnos-dia";
    }

    @PostMapping("/turnos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                 @RequestParam Turno.EstadoTurno estado,
                                 RedirectAttributes ra) {
        turnoServicio.cambiarEstado(id, estado);
        ra.addFlashAttribute("exito", "Estado actualizado correctamente.");
        return "redirect:/admin/panel";
    }

    @PostMapping("/turnos/{id}/eliminar")
    public String eliminarTurno(@PathVariable Long id, RedirectAttributes ra) {
        turnoServicio.eliminarTurno(id);
        ra.addFlashAttribute("exito", "Turno eliminado.");
        return "redirect:/admin/panel";
    }

    // --- Disponibilidad ---

    @GetMapping("/disponibilidad")
    public String disponibilidad(Model model) {
        model.addAttribute("bloques", turnoServicio.getTodosLosBloques());
        model.addAttribute("diasSemana", DayOfWeek.values());
        return "admin/disponibilidad";
    }

    @PostMapping("/disponibilidad/agregar")
    public String agregarBloque(
            @RequestParam DayOfWeek diaSemana,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
            @RequestParam int duracionMinutos,
            RedirectAttributes ra) {

        BloqueTurno bloque = new BloqueTurno();
        bloque.setDiaSemana(diaSemana);
        bloque.setHoraInicio(horaInicio);
        bloque.setHoraFin(horaFin);
        bloque.setDuracionMinutos(duracionMinutos);
        bloque.setActivo(true);
        turnoServicio.guardarBloque(bloque);

        ra.addFlashAttribute("exito", "Bloque de disponibilidad agregado.");
        return "redirect:/admin/disponibilidad";
    }

    @PostMapping("/disponibilidad/{id}/eliminar")
    public String eliminarBloque(@PathVariable Long id, RedirectAttributes ra) {
        turnoServicio.eliminarBloque(id);
        ra.addFlashAttribute("exito", "Bloque eliminado.");
        return "redirect:/admin/disponibilidad";
    }

    @PostMapping("/disponibilidad/{id}/toggle")
    public String toggleBloque(@PathVariable Long id, RedirectAttributes ra) {
        turnoServicio.getTodosLosBloques().stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .ifPresent(b -> {
                b.setActivo(!b.isActivo());
                turnoServicio.guardarBloque(b);
            });
        return "redirect:/admin/disponibilidad";
    }
}
