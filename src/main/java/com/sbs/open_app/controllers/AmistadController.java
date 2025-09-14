package com.sbs.open_app.controllers;

import com.sbs.open_app.dto.AmistadDTO;
import com.sbs.open_app.dto.UsuarioBusquedaDTO;
import com.sbs.open_app.servicios.AmistadService;
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
@RequestMapping("/api/amistades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AmistadController {
    
    private static final Logger logger = LoggerFactory.getLogger(AmistadController.class);
    
    private final AmistadService amistadService;
    
    /**
     * Enviar solicitud de amistad
     * POST /api/amistades/solicitud
     */
    @PostMapping("/solicitud")
    public ResponseEntity<?> enviarSolicitud(@RequestBody SolicitudAmistadRequest request) {
        try {
            logger.info("Enviando solicitud de amistad de {} a {}", request.getUsuarioId(), request.getAmigoId());
            
            AmistadDTO amistad = amistadService.enviarSolicitud(
                request.getUsuarioId(),
                request.getAmigoId(),
                request.getMensaje()
            );
            
            return ResponseEntity.ok(amistad);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error en solicitud de amistad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno enviando solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Aceptar solicitud de amistad
     * PUT /api/amistades/{solicitudId}/aceptar
     */
    @PutMapping("/{solicitudId}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long usuarioId) {
        try {
            logger.info("Aceptando solicitud {} por usuario {}", solicitudId, usuarioId);
            
            AmistadDTO amistad = amistadService.aceptarSolicitud(usuarioId, solicitudId);
            
            return ResponseEntity.ok(amistad);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error aceptando solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno aceptando solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Rechazar solicitud de amistad
     * PUT /api/amistades/{solicitudId}/rechazar
     */
    @PutMapping("/{solicitudId}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long usuarioId) {
        try {
            logger.info("Rechazando solicitud {} por usuario {}", solicitudId, usuarioId);
            
            amistadService.rechazarSolicitud(usuarioId, solicitudId);
            
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada exitosamente"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error rechazando solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno rechazando solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Eliminar amistad
     * DELETE /api/amistades/usuario/{usuarioId}/amigo/{amigoId}
     */
    @DeleteMapping("/usuario/{usuarioId}/amigo/{amigoId}")
    public ResponseEntity<?> eliminarAmistad(
            @PathVariable Long usuarioId,
            @PathVariable Long amigoId) {
        try {
            logger.info("Eliminando amistad entre {} y {}", usuarioId, amigoId);
            
            amistadService.eliminarAmistad(usuarioId, amigoId);
            
            return ResponseEntity.ok(Map.of("mensaje", "Amistad eliminada exitosamente"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error eliminando amistad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno eliminando amistad", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Obtener lista de amigos
     * GET /api/amistades/usuario/{usuarioId}/amigos
     */
    @GetMapping("/usuario/{usuarioId}/amigos")
    public ResponseEntity<?> obtenerAmigos(@PathVariable Long usuarioId) {
        try {
            logger.info("Obteniendo amigos del usuario: {}", usuarioId);
            
            List<AmistadDTO> amigos = amistadService.obtenerAmigos(usuarioId);
            
            return ResponseEntity.ok(amigos);
            
        } catch (Exception e) {
            logger.error("Error obteniendo amigos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Obtener solicitudes pendientes recibidas
     * GET /api/amistades/usuario/{usuarioId}/solicitudes/recibidas
     */
    @GetMapping("/usuario/{usuarioId}/solicitudes/recibidas")
    public ResponseEntity<?> obtenerSolicitudesRecibidas(@PathVariable Long usuarioId) {
        try {
            logger.info("Obteniendo solicitudes recibidas para: {}", usuarioId);
            
            List<AmistadDTO> solicitudes = amistadService.obtenerSolicitudesPendientes(usuarioId);
            
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            logger.error("Error obteniendo solicitudes recibidas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Obtener solicitudes pendientes enviadas
     * GET /api/amistades/usuario/{usuarioId}/solicitudes/enviadas
     */
    @GetMapping("/usuario/{usuarioId}/solicitudes/enviadas")
    public ResponseEntity<?> obtenerSolicitudesEnviadas(@PathVariable Long usuarioId) {
        try {
            logger.info("Obteniendo solicitudes enviadas por: {}", usuarioId);
            
            List<AmistadDTO> solicitudes = amistadService.obtenerSolicitudesEnviadas(usuarioId);
            
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            logger.error("Error obteniendo solicitudes enviadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Buscar usuarios para agregar
     * GET /api/amistades/usuario/{usuarioId}/buscar?q={termino}
     */
    @GetMapping("/usuario/{usuarioId}/buscar")
    public ResponseEntity<?> buscarUsuarios(
            @PathVariable Long usuarioId,
            @RequestParam("q") String busqueda) {
        try {
            logger.info("Buscando usuarios para {}: {}", usuarioId, busqueda);
            
            List<UsuarioBusquedaDTO> usuarios = amistadService.buscarUsuarios(usuarioId, busqueda);
            
            return ResponseEntity.ok(usuarios);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error en búsqueda: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error buscando usuarios", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Verificar estado de relación
     * GET /api/amistades/usuario/{usuarioId}/estado/{amigoId}
     */
    @GetMapping("/usuario/{usuarioId}/estado/{amigoId}")
    public ResponseEntity<?> verificarEstadoRelacion(
            @PathVariable Long usuarioId,
            @PathVariable Long amigoId) {
        try {
            logger.info("Verificando estado entre {} y {}", usuarioId, amigoId);
            
            AmistadService.EstadoRelacion estado = amistadService.verificarEstadoRelacion(usuarioId, amigoId);
            
            return ResponseEntity.ok(estado);
            
        } catch (Exception e) {
            logger.error("Error verificando estado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    /**
     * Obtener estadísticas de amistad
     * GET /api/amistades/usuario/{usuarioId}/estadisticas
     */
    @GetMapping("/usuario/{usuarioId}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable Long usuarioId) {
        try {
            logger.info("Obteniendo estadísticas para: {}", usuarioId);
            
            AmistadService.EstadisticasAmistad estadisticas = amistadService.obtenerEstadisticas(usuarioId);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    // DTOs para requests
    public static class SolicitudAmistadRequest {
        private Long usuarioId;
        private Long amigoId;
        private String mensaje;
        
        // Constructors
        public SolicitudAmistadRequest() {}
        
        public SolicitudAmistadRequest(Long usuarioId, Long amigoId, String mensaje) {
            this.usuarioId = usuarioId;
            this.amigoId = amigoId;
            this.mensaje = mensaje;
        }
        
        // Getters y Setters
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        
        public Long getAmigoId() { return amigoId; }
        public void setAmigoId(Long amigoId) { this.amigoId = amigoId; }
        
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}