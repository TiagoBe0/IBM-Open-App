package com.sbs.open_app.controllers;

import com.sbs.open_app.dto.ComentarioLibroDTO;
import com.sbs.open_app.servicios.LectorLibroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class LectorLibroController {

    @Autowired
    private LectorLibroService lectorService;

    /**
     * Página principal del lector
     */
    @GetMapping("/lector")
    public String paginaLector(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("usuario", auth.getPrincipal());
        }
        return "lector";
    }

    /**
     * API: Obtiene el libro completo
     */
    @GetMapping("/api/lector/libro")
    @ResponseBody
    public ResponseEntity<?> obtenerLibro() {
        try {
            Map<String, Object> libro = lectorService.leerLibro();
            return ResponseEntity.ok(libro);
        } catch (IOException e) {
            log.error("Error al leer el libro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo cargar el libro: " + e.getMessage()));
        }
    }

    /**
     * API: Obtiene una sección específica
     */
    @GetMapping("/api/lector/seccion/{documento}/{seccion}")
    @ResponseBody
    public ResponseEntity<?> obtenerSeccion(
            @PathVariable Integer documento,
            @PathVariable Integer seccion) {
        try {
            Map<String, Object> seccionData = lectorService.obtenerSeccion(documento, seccion);
            if (seccionData != null) {
                return ResponseEntity.ok(seccionData);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Sección no encontrada"));
            }
        } catch (IOException e) {
            log.error("Error al obtener la sección", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar la sección: " + e.getMessage()));
        }
    }

    /**
     * API: Guarda un comentario
     */
    @PostMapping("/api/lector/comentario")
    @ResponseBody
    public ResponseEntity<?> guardarComentario(@RequestBody ComentarioLibroDTO comentarioDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Debes iniciar sesión para comentar"));
            }

            Long usuarioId = ((com.sbs.open_app.entidades.Usuario) auth.getPrincipal()).getId();
            ComentarioLibroDTO guardado = lectorService.guardarComentario(usuarioId, comentarioDTO);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            log.error("Error al guardar comentario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar el comentario: " + e.getMessage()));
        }
    }

    /**
     * API: Obtiene comentarios de una sección
     */
    @GetMapping("/api/lector/comentarios/{documento}/{seccion}")
    @ResponseBody
    public ResponseEntity<?> obtenerComentariosSeccion(
            @PathVariable Integer documento,
            @PathVariable Integer seccion) {
        try {
            List<ComentarioLibroDTO> comentarios = lectorService.obtenerComentariosSeccion(documento, seccion);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            log.error("Error al obtener comentarios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar comentarios: " + e.getMessage()));
        }
    }

    /**
     * API: Obtiene comentarios de un párrafo específico
     */
    @GetMapping("/api/lector/comentarios/{documento}/{seccion}/{parrafo}")
    @ResponseBody
    public ResponseEntity<?> obtenerComentariosParrafo(
            @PathVariable Integer documento,
            @PathVariable Integer seccion,
            @PathVariable Integer parrafo) {
        try {
            List<ComentarioLibroDTO> comentarios = lectorService.obtenerComentariosParrafo(documento, seccion, parrafo);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            log.error("Error al obtener comentarios del párrafo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar comentarios: " + e.getMessage()));
        }
    }

    /**
     * API: Obtiene comentarios del usuario actual
     */
    @GetMapping("/api/lector/mis-comentarios")
    @ResponseBody
    public ResponseEntity<?> obtenerMisComentarios() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Debes iniciar sesión"));
            }

            Long usuarioId = ((com.sbs.open_app.entidades.Usuario) auth.getPrincipal()).getId();
            List<ComentarioLibroDTO> comentarios = lectorService.obtenerComentariosUsuario(usuarioId);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            log.error("Error al obtener comentarios del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar comentarios: " + e.getMessage()));
        }
    }

    /**
     * API: Elimina un comentario
     */
    @DeleteMapping("/api/lector/comentario/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarComentario(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Debes iniciar sesión"));
            }

            Long usuarioId = ((com.sbs.open_app.entidades.Usuario) auth.getPrincipal()).getId();
            lectorService.eliminarComentario(id, usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Comentario eliminado exitosamente"));
        } catch (Exception e) {
            log.error("Error al eliminar comentario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar comentario: " + e.getMessage()));
        }
    }

    /**
     * API: Actualiza un comentario
     */
    @PutMapping("/api/lector/comentario/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarComentario(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Debes iniciar sesión"));
            }

            String nuevoContenido = body.get("contenido");
            if (nuevoContenido == null || nuevoContenido.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El contenido no puede estar vacío"));
            }

            Long usuarioId = ((com.sbs.open_app.entidades.Usuario) auth.getPrincipal()).getId();
            ComentarioLibroDTO actualizado = lectorService.actualizarComentario(id, usuarioId, nuevoContenido);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            log.error("Error al actualizar comentario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar comentario: " + e.getMessage()));
        }
    }
}
