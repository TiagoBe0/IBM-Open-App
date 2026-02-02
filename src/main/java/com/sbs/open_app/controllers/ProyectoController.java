package com.sbs.open_app.controllers;

import com.sbs.open_app.dto.ProyectoDTO;
import com.sbs.open_app.servicios.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyecto")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ProyectoController {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoController.class);
    private final ProyectoService proyectoService;

    @PostMapping("/registrar")
    public ResponseEntity<?> crear(@RequestBody ProyectoDTO proyectoDTO) {
        logger.info("Creando proyecto: {}", proyectoDTO.getNombre());

        try {
            if (proyectoDTO.getUsuarioId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El campo usuarioId es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            if (proyectoDTO.getNombre() == null || proyectoDTO.getNombre().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El nombre del proyecto es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            ProyectoDTO nuevoProyecto = proyectoService.crear(proyectoDTO);
            logger.info("Proyecto creado exitosamente con ID: {}", nuevoProyecto.getId());
            return new ResponseEntity<>(nuevoProyecto, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            logger.error("Error en el controlador: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        logger.info("Obteniendo proyecto ID: {}", id);

        try {
            ProyectoDTO proyecto = proyectoService.obtenerPorId(id);
            return ResponseEntity.ok(proyecto);
        } catch (RuntimeException e) {
            logger.error("Error obteniendo proyecto: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPorUsuario(@PathVariable Long usuarioId) {
        logger.info("Obteniendo proyectos del usuario ID: {}", usuarioId);

        try {
            List<ProyectoDTO> proyectos = proyectoService.obtenerPorUsuario(usuarioId);
            logger.info("Se encontraron {} proyectos", proyectos.size());
            return ResponseEntity.ok(proyectos);
        } catch (Exception e) {
            logger.error("Error obteniendo proyectos: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener proyectos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ProyectoDTO proyectoDTO) {
        logger.info("Actualizando proyecto ID: {}", id);

        try {
            ProyectoDTO proyectoActualizado = proyectoService.actualizar(id, proyectoDTO);
            return ResponseEntity.ok(proyectoActualizado);
        } catch (RuntimeException e) {
            logger.error("Error actualizando proyecto: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        logger.info("Eliminando proyecto ID: {}", id);

        try {
            proyectoService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Proyecto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error eliminando proyecto: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
