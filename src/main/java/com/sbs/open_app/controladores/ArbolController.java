package com.sbs.open_app.controladores;


import com.sbs.open_app.dto.ArbolDTO;
import com.sbs.open_app.servicios.ArbolService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/arboles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ArbolController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArbolController.class);
    
    private final ArbolService arbolService;
    
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ArbolDTO arbolDTO) {
        logger.info("=== PETICIÓN POST RECIBIDA ===");
        logger.info("Datos del body: {}", arbolDTO);
        
        try {
            // Validación manual
            if (arbolDTO.getUsuarioId() == null) {
                logger.error("usuarioId es null en el request");
                Map<String, String> error = new HashMap<>();
                error.put("error", "El campo usuarioId es obligatorio");
                error.put("campo", "usuarioId");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (arbolDTO.getA() == null || arbolDTO.getA().trim().isEmpty()) {
                logger.error("Campo A es null o vacío");
                Map<String, String> error = new HashMap<>();
                error.put("error", "El campo A es obligatorio");
                error.put("campo", "a");
                return ResponseEntity.badRequest().body(error);
            }
            
            
            logger.info("Validación pasada, creando árbol...");
            ArbolDTO nuevoArbol = arbolService.crear(arbolDTO);
            
            logger.info("Árbol creado exitosamente con ID: {}", nuevoArbol.getId());
            return new ResponseEntity<>(nuevoArbol, HttpStatus.CREATED);
            
        } catch (RuntimeException e) {
            logger.error("Error en el controlador: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("tipo", "RuntimeException");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            logger.error("Error inesperado: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error inesperado: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        logger.info("GET request para árbol ID: {}", id);
        
        try {
            ArbolDTO arbol = arbolService.obtenerPorId(id);
            return ResponseEntity.ok(arbol);
        } catch (RuntimeException e) {
            logger.error("Error obteniendo árbol: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPorUsuario(@PathVariable Long usuarioId) {
        logger.info("GET request para árboles del usuario ID: {}", usuarioId);
        
        try {
            List<ArbolDTO> arboles = arbolService.obtenerPorUsuario(usuarioId);
            logger.info("Retornando {} árboles", arboles.size());
            return ResponseEntity.ok(arboles);
        } catch (Exception e) {
            logger.error("Error obteniendo árboles del usuario: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ArbolDTO arbolDTO) {
        logger.info("PUT request para árbol ID: {}", id);
        
        try {
            ArbolDTO arbolActualizado = arbolService.actualizar(id, arbolDTO);
            return ResponseEntity.ok(arbolActualizado);
        } catch (RuntimeException e) {
            logger.error("Error actualizando árbol: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        logger.info("DELETE request para árbol ID: {}", id);
        
        try {
            arbolService.eliminar(id);
            logger.info("Árbol eliminado exitosamente");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error eliminando árbol: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // Endpoint de prueba para verificar conectividad
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        logger.info("Test endpoint llamado");
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "El controlador de árboles está funcionando");
        return ResponseEntity.ok(response);
    }
}