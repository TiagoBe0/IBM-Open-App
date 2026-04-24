package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.Turno;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import com.sbs.open_app.servicios.TurnoServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class CalendarioController {

    private final TurnoServicio turnoServicio;
    private final UsuarioRepositorio usuarioRepositorio;

    public CalendarioController(TurnoServicio turnoServicio, UsuarioRepositorio usuarioRepositorio) {
        this.turnoServicio = turnoServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/turnos";
    }

    @GetMapping("/turnos")
    public String calendario(Model model, Authentication auth) {
        LocalDate hoy = LocalDate.now();
        Map<LocalDate, List<LocalTime>> disponibilidad = turnoServicio.getSlotsDisponibles(hoy, hoy.plusDays(30));
        model.addAttribute("disponibilidad", disponibilidad);
        model.addAttribute("hoy", hoy);
        agregarInfoUsuario(model, auth);
        return "calendario";
    }

    @GetMapping("/api/turnos/disponibilidad")
    @ResponseBody
    public ResponseEntity<Map<LocalDate, List<LocalTime>>> getDisponibilidad(
            @RequestParam(defaultValue = "0") int offset) {
        LocalDate desde = LocalDate.now().plusDays(offset * 30L);
        Map<LocalDate, List<LocalTime>> disponibilidad = turnoServicio.getSlotsDisponibles(desde, desde.plusDays(29));
        return ResponseEntity.ok(disponibilidad);
    }

    @GetMapping("/turnos/reservar")
    public String mostrarFormulario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String hora,
            Model model,
            Authentication auth) {

        LocalTime horaLocal = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", horaLocal);
        model.addAttribute("fechaFormateada",
                fecha.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM yyyy", new Locale("es", "AR"))));

        // Precargar datos si el paciente está autenticado con Google
        agregarInfoUsuario(model, auth);
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
            RedirectAttributes ra) {

        try {
            LocalTime horaLocal = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            Turno turno = turnoServicio.reservarTurno(
                    nombrePaciente.trim(), emailPaciente.trim().toLowerCase(),
                    telefonoPaciente, motivo, fecha, horaLocal);
            ra.addFlashAttribute("turno", turno);
            return "redirect:/turnos/confirmacion/" + turno.getId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/turnos";
        }
    }

    @GetMapping("/turnos/confirmacion/{id}")
    public String confirmacion(@PathVariable Long id, Model model, Authentication auth) {
        turnoServicio.getTurnoPorId(id).ifPresent(t -> model.addAttribute("turno", t));
        agregarInfoUsuario(model, auth);
        return "confirmacion";
    }

    @GetMapping("/turnos/mis-turnos")
    public String misTurnos(@RequestParam(required = false) String email, Model model, Authentication auth) {
        // Si el paciente está autenticado con Google, mostrar sus turnos automáticamente
        String emailEfectivo = email;
        if (emailEfectivo == null || emailEfectivo.isBlank()) {
            emailEfectivo = extraerEmail(auth);
        }

        if (emailEfectivo != null && !emailEfectivo.isBlank()) {
            model.addAttribute("turnos", turnoServicio.getTurnosPorEmail(emailEfectivo.toLowerCase()));
            model.addAttribute("email", emailEfectivo);
        }

        agregarInfoUsuario(model, auth);
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Extrae el email del principal sin importar si es OAuth2 o form-login. */
    private String extraerEmail(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        if (auth.getPrincipal() instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("email");
        }
        if (auth.getPrincipal() instanceof Usuario u) {
            return u.getEmail();
        }
        return null;
    }

    /** Carga en el model los datos del usuario logueado (nombre, email, foto, rol). */
    private void agregarInfoUsuario(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return;

        String email = extraerEmail(auth);
        if (email == null) return;

        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);
        usuarioOpt.ifPresent(u -> {
            model.addAttribute("usuarioLogueado", u);
            model.addAttribute("emailLogueado", u.getEmail());
            model.addAttribute("nombreLogueado", u.getNombreCompleto());
            model.addAttribute("fotoLogueado", u.getFotoUrl());
            model.addAttribute("esAdmin", u.esAdmin());
        });

        // Si aún no está en la BD (raro, pero por si acaso), sacamos del OAuth2
        if (!model.containsAttribute("emailLogueado") && auth.getPrincipal() instanceof OAuth2User oAuth2User) {
            model.addAttribute("emailLogueado", email);
            model.addAttribute("nombreLogueado", oAuth2User.getAttribute("name"));
            model.addAttribute("fotoLogueado", oAuth2User.getAttribute("picture"));
            model.addAttribute("esAdmin", false);
        }
    }
}
