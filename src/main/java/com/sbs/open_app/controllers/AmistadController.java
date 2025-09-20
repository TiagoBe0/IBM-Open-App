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

import jakarta.validation.Valid;
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
    public ResponseEntity<?> enviarSolicitud(@Valid @RequestBody SolicitudAmistadRequest request) {
        try {
            logger.info("Enviando solicitud de amistad de {} a {}", request.getUsuarioId(), request.getAmigoId());
            
            AmistadDTO amistad = amistadService.enviarSolicitud(
                request.getUsuarioId(),
                request.getAmigoId(),
                request.getMensaje()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Solicitud enviada exitosamente");
            response.put("solicitud", amistad);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error en solicitud de amistad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (RuntimeException e) {
            logger.error("Error de negocio enviando solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno enviando solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
        }
    }
    
    /**
     * Aceptar solicitud de amistad - CORREGIDO
     * PUT /api/amistades/{solicitudId}/aceptar?usuarioId={usuarioId}
     */
    @PutMapping("/{solicitudId}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long usuarioId) {
        try {
            logger.info("Usuario {} intentando aceptar solicitud {}", usuarioId, solicitudId);
            
            // Validar parámetros
            if (solicitudId == null || solicitudId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de solicitud inválido"));
            }
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            AmistadDTO amistad = amistadService.aceptarSolicitud(usuarioId, solicitudId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Solicitud aceptada exitosamente");
            response.put("amistad", amistad);
            
            logger.info("Solicitud {} aceptada exitosamente por usuario {}", solicitudId, usuarioId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error aceptando solicitud {}: {}", solicitudId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (RuntimeException e) {
            logger.error("Error de negocio aceptando solicitud {}: {}", solicitudId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno aceptando solicitud {}", solicitudId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
        }
    }
    
    /**
     * Rechazar solicitud de amistad - CORREGIDO
     * PUT /api/amistades/{solicitudId}/rechazar?usuarioId={usuarioId}
     */
    @PutMapping("/{solicitudId}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam Long usuarioId) {
        try {
            logger.info("Usuario {} intentando rechazar solicitud {}", usuarioId, solicitudId);
            
            // Validar parámetros
            if (solicitudId == null || solicitudId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de solicitud inválido"));
            }
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            amistadService.rechazarSolicitud(usuarioId, solicitudId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Solicitud rechazada exitosamente");
            
            logger.info("Solicitud {} rechazada exitosamente por usuario {}", solicitudId, usuarioId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error rechazando solicitud {}: {}", solicitudId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (RuntimeException e) {
            logger.error("Error de negocio rechazando solicitud {}: {}", solicitudId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno rechazando solicitud {}", solicitudId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
        }
    }
    
    /**
     * Eliminar amistad - CORREGIDO
     * DELETE /api/amistades/usuario/{usuarioId}/amigo/{amigoId}
     */
    @DeleteMapping("/usuario/{usuarioId}/amigo/{amigoId}")
    public ResponseEntity<?> eliminarAmistad(
            @PathVariable Long usuarioId,
            @PathVariable Long amigoId) {
        try {
            logger.info("Usuario {} intentando eliminar amistad con usuario {}", usuarioId, amigoId);
            
            // Validar parámetros
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            if (amigoId == null || amigoId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de amigo inválido"));
            }
            
            amistadService.eliminarAmistad(usuarioId, amigoId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Amistad eliminada exitosamente");
            
            logger.info("Amistad eliminada exitosamente entre usuarios {} y {}", usuarioId, amigoId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error eliminando amistad entre {} y {}: {}", usuarioId, amigoId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (RuntimeException e) {
            logger.error("Error de negocio eliminando amistad entre {} y {}: {}", usuarioId, amigoId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error interno eliminando amistad entre {} y {}", usuarioId, amigoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            List<AmistadDTO> amigos = amistadService.obtenerAmigos(usuarioId);
            
            return ResponseEntity.ok(amigos);
            
        } catch (Exception e) {
            logger.error("Error obteniendo amigos para usuario {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            List<AmistadDTO> solicitudes = amistadService.obtenerSolicitudesPendientes(usuarioId);
            
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            logger.error("Error obteniendo solicitudes recibidas para usuario {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            List<AmistadDTO> solicitudes = amistadService.obtenerSolicitudesEnviadas(usuarioId);
            
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            logger.error("Error obteniendo solicitudes enviadas para usuario {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            if (busqueda == null || busqueda.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Término de búsqueda requerido"));
            }
            
            List<UsuarioBusquedaDTO> usuarios = amistadService.buscarUsuarios(usuarioId, busqueda);
            
            return ResponseEntity.ok(usuarios);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error en búsqueda para usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error buscando usuarios para {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0 || amigoId == null || amigoId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("IDs de usuario inválidos"));
            }
            
            AmistadService.EstadoRelacion estado = amistadService.verificarEstadoRelacion(usuarioId, amigoId);
            
            return ResponseEntity.ok(estado);
            
        } catch (Exception e) {
            logger.error("Error verificando estado entre {} y {}", usuarioId, amigoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
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
            
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("ID de usuario inválido"));
            }
            
            AmistadService.EstadisticasAmistad estadisticas = amistadService.obtenerEstadisticas(usuarioId);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas para usuario {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor"));
        }
    }
    
    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, Object> createErrorResponse(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", mensaje);
        error.put("timestamp", System.currentTimeMillis());
        return error;
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
        
        @Override
        public String toString() {
            return String.format("SolicitudAmistadRequest{usuarioId=%d, amigoId=%d, mensaje='%s'}", 
                usuarioId, amigoId, mensaje);
        }
    }
}