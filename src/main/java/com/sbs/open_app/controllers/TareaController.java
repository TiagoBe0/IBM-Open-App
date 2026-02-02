package com.sbs.open_app.controllers;

import com.sbs.open_app.dto.TareaDTO;
import com.sbs.open_app.servicios.TareaService;
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
@RequestMapping("/api/tarea")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TareaController {

    private static final Logger logger = LoggerFactory.getLogger(TareaController.class);
    private final TareaService tareaService;

    @PostMapping("/registrar")
    public ResponseEntity<?> crear(@RequestBody TareaDTO tareaDTO) {
        logger.info("Creando tarea: {}", tareaDTO.getTitulo());

        try {
            if (tareaDTO.getProyectoId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El campo proyectoId es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            if (tareaDTO.getTitulo() == null || tareaDTO.getTitulo().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El título de la tarea es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            TareaDTO nuevaTarea = tareaService.crear(tareaDTO);
            logger.info("Tarea creada exitosamente con ID: {}", nuevaTarea.getId());
            return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            logger.error("Error en el controlador: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        logger.info("Obteniendo tarea ID: {}", id);

        try {
            TareaDTO tarea = tareaService.obtenerPorId(id);
            return ResponseEntity.ok(tarea);
        } catch (RuntimeException e) {
            logger.error("Error obteniendo tarea: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<?> obtenerPorProyecto(@PathVariable Long proyectoId) {
        logger.info("Obteniendo tareas del proyecto ID: {}", proyectoId);

        try {
            List<TareaDTO> tareas = tareaService.obtenerPorProyecto(proyectoId);
            logger.info("Se encontraron {} tareas", tareas.size());
            return ResponseEntity.ok(tareas);
        } catch (Exception e) {
            logger.error("Error obteniendo tareas: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener tareas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody TareaDTO tareaDTO) {
        logger.info("Actualizando tarea ID: {}", id);

        try {
            TareaDTO tareaActualizada = tareaService.actualizar(id, tareaDTO);
            return ResponseEntity.ok(tareaActualizada);
        } catch (RuntimeException e) {
            logger.error("Error actualizando tarea: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        logger.info("Eliminando tarea ID: {}", id);

        try {
            tareaService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Tarea eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error eliminando tarea: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
