package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.AmistadDTO;
import com.sbs.open_app.dto.UsuarioBusquedaDTO;
import com.sbs.open_app.entidades.Amistad;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.AmistadRepository;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AmistadService {
    
    private static final Logger logger = LoggerFactory.getLogger(AmistadService.class);
    
    private final AmistadRepository amistadRepository;
    private final UsuarioRepositorio usuarioRepository;
    
    /**
     * Enviar solicitud de amistad
     */
    public AmistadDTO enviarSolicitud(Long usuarioId, Long amigoId, String mensaje) {
        logger.info("Enviando solicitud de amistad de {} a {}", usuarioId, amigoId);
        
        // Validaciones básicas
        if (usuarioId.equals(amigoId)) {
            throw new IllegalArgumentException("No puedes enviarte solicitud a ti mismo");
        }
        
        // Verificar que los usuarios existen
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Usuario amigo = usuarioRepository.findById(amigoId)
            .orElseThrow(() -> new RuntimeException("Usuario destino no encontrado"));
        
        // Verificar si ya existe una relación
        Optional<Amistad> relacionExistente = amistadRepository.findRelationBetweenUsers(usuarioId, amigoId);
        if (relacionExistente.isPresent()) {
            Amistad amistad = relacionExistente.get();
            switch (amistad.getEstado()) {
                case ACEPTADA:
                    throw new IllegalArgumentException("Ya son amigos");
                case PENDIENTE:
                    throw new IllegalArgumentException("Ya existe una solicitud pendiente");
                case BLOQUEADA:
                    throw new IllegalArgumentException("Esta relación está bloqueada");
                case RECHAZADA:
                    // Permitir nueva solicitud después de rechazo
                    amistad.setEstado(Amistad.EstadoAmistad.PENDIENTE);
                    amistad.setFechaSolicitud(LocalDateTime.now());
                    amistad.setMensajeSolicitud(mensaje);
                    amistad.setFechaAceptacion(null);
                    Amistad actualizada = amistadRepository.save(amistad);
                    return convertirADTO(actualizada);
            }
        }
        
        // Crear nueva solicitud
        Amistad nuevaAmistad = new Amistad();
        nuevaAmistad.setUsuario(usuario);
        nuevaAmistad.setAmigo(amigo);
        nuevaAmistad.setEstado(Amistad.EstadoAmistad.PENDIENTE);
        nuevaAmistad.setMensajeSolicitud(mensaje);
        nuevaAmistad.setFechaSolicitud(LocalDateTime.now());
        
        Amistad amistadGuardada = amistadRepository.save(nuevaAmistad);
        logger.info("Solicitud enviada exitosamente");
        
        return convertirADTO(amistadGuardada);
    }
    
    /**
     * Aceptar solicitud de amistad
     */
    public AmistadDTO aceptarSolicitud(Long usuarioId, Long solicitudId) {
        logger.info("Aceptando solicitud de amistad ID: {} por usuario: {}", solicitudId, usuarioId);
        
        Amistad amistad = amistadRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        // Verificar que el usuario puede aceptar esta solicitud
        if (!amistad.getAmigo().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para aceptar esta solicitud");
        }
        
        if (amistad.getEstado() != Amistad.EstadoAmistad.PENDIENTE) {
            throw new IllegalArgumentException("Esta solicitud ya fue procesada");
        }
        
        // Aceptar solicitud
        amistad.setEstado(Amistad.EstadoAmistad.ACEPTADA);
        amistad.setFechaAceptacion(LocalDateTime.now());
        
        Amistad amistadAceptada = amistadRepository.save(amistad);
        logger.info("Solicitud aceptada exitosamente");
        
        return convertirADTO(amistadAceptada);
    }
    
    /**
     * Rechazar solicitud de amistad
     */
    public void rechazarSolicitud(Long usuarioId, Long solicitudId) {
        logger.info("Rechazando solicitud de amistad ID: {} por usuario: {}", solicitudId, usuarioId);
        
        Amistad amistad = amistadRepository.findById(solicitudId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        // Verificar permisos
        if (!amistad.getAmigo().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para rechazar esta solicitud");
        }
        
        if (amistad.getEstado() != Amistad.EstadoAmistad.PENDIENTE) {
            throw new IllegalArgumentException("Esta solicitud ya fue procesada");
        }
        
        // Rechazar solicitud
        amistad.setEstado(Amistad.EstadoAmistad.RECHAZADA);
        amistadRepository.save(amistad);
        
        logger.info("Solicitud rechazada exitosamente");
    }
    
    /**
     * Eliminar amistad (unfriend)
     */
    public void eliminarAmistad(Long usuarioId, Long amigoId) {
        logger.info("Eliminando amistad entre {} y {}", usuarioId, amigoId);
        
        Amistad amistad = amistadRepository.findRelationBetweenUsers(usuarioId, amigoId)
            .orElseThrow(() -> new RuntimeException("No existe relación entre estos usuarios"));
        
        // Solo se puede eliminar si son amigos o si el usuario fue quien envió/recibió la solicitud
        boolean puedeEliminar = amistad.getUsuario().getId().equals(usuarioId) || 
                               amistad.getAmigo().getId().equals(usuarioId);
        
        if (!puedeEliminar) {
            throw new IllegalArgumentException("No tienes permisos para eliminar esta relación");
        }
        
        amistadRepository.delete(amistad);
        logger.info("Amistad eliminada exitosamente");
    }
    
    /**
     * Obtener lista de amigos
     */
    @Transactional(readOnly = true)
    public List<AmistadDTO> obtenerAmigos(Long usuarioId) {
        logger.info("Obteniendo amigos del usuario: {}", usuarioId);
        
        List<Amistad> amistades = amistadRepository.findActiveFreindsByUser(usuarioId);
        
        return amistades.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener solicitudes pendientes recibidas
     */
    @Transactional(readOnly = true)
    public List<AmistadDTO> obtenerSolicitudesPendientes(Long usuarioId) {
        logger.info("Obteniendo solicitudes pendientes para usuario: {}", usuarioId);
        
        List<Amistad> solicitudes = amistadRepository.findPendingRequestsReceived(usuarioId);
        
        return solicitudes.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener solicitudes enviadas
     */
    @Transactional(readOnly = true)
    public List<AmistadDTO> obtenerSolicitudesEnviadas(Long usuarioId) {
        logger.info("Obteniendo solicitudes enviadas por usuario: {}", usuarioId);
        
        List<Amistad> solicitudes = amistadRepository.findPendingRequestsSent(usuarioId);
        
        return solicitudes.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Buscar usuarios para agregar como amigos
     */
    @Transactional(readOnly = true)
    public List<UsuarioBusquedaDTO> buscarUsuarios(Long usuarioId, String busqueda) {
        logger.info("Buscando usuarios potenciales para: {} con término: {}", usuarioId, busqueda);
        
        if (busqueda == null || busqueda.trim().length() < 2) {
            throw new IllegalArgumentException("El término de búsqueda debe tener al menos 2 caracteres");
        }
        
        List<Usuario> usuarios = amistadRepository.findPotentialFreinds(busqueda.trim(), usuarioId);
        
        return usuarios.stream()
            .map(usuario -> convertirAUsuarioBusquedaDTO(usuario, usuarioId))
            .collect(Collectors.toList());
    }
    
    /**
     * Verificar estado de relación entre dos usuarios
     */
    @Transactional(readOnly = true)
    public EstadoRelacion verificarEstadoRelacion(Long usuarioId, Long amigoId) {
        Optional<Amistad> relacion = amistadRepository.findRelationBetweenUsers(usuarioId, amigoId);
        
        if (relacion.isEmpty()) {
            return new EstadoRelacion("NINGUNA", false, false);
        }
        
        Amistad amistad = relacion.get();
        boolean solicitudEnviada = amistad.getUsuario().getId().equals(usuarioId);
        boolean solicitudRecibida = amistad.getAmigo().getId().equals(usuarioId);
        
        return new EstadoRelacion(
            amistad.getEstado().name(),
            solicitudEnviada,
            solicitudRecibida
        );
    }
    
    /**
     * Obtener estadísticas de amistad
     */
    @Transactional(readOnly = true)
    public EstadisticasAmistad obtenerEstadisticas(Long usuarioId) {
        Long totalAmigos = amistadRepository.countActiveFreindsByUser(usuarioId);
        Long solicitudesRecibidas = (long) amistadRepository.findPendingRequestsReceived(usuarioId).size();
        Long solicitudesEnviadas = (long) amistadRepository.findPendingRequestsSent(usuarioId).size();
        
        return new EstadisticasAmistad(totalAmigos, solicitudesRecibidas, solicitudesEnviadas);
    }
    
    // Métodos de conversión
    private AmistadDTO convertirADTO(Amistad amistad) {
        AmistadDTO dto = new AmistadDTO();
        dto.setId(amistad.getId());
        dto.setUsuarioId(amistad.getUsuario().getId());
        dto.setAmigoId(amistad.getAmigo().getId());
        dto.setEstado(amistad.getEstado().name());
        dto.setFechaSolicitud(amistad.getFechaSolicitud());
        dto.setFechaAceptacion(amistad.getFechaAceptacion());
        dto.setMensajeSolicitud(amistad.getMensajeSolicitud());
        
        // Información del amigo
        Usuario amigo = amistad.getAmigo();
        dto.setAmigoNombre(amigo.getNombre());
        dto.setAmigoApellido(amigo.getApellido());
        dto.setAmigoUsername(amigo.getUsername());
        dto.setAmigoEmail(amigo.getEmail());
        dto.setAmigoFotoPerfil(amigo.getFotoPerfil());
        
        // Información del usuario
        Usuario usuario = amistad.getUsuario();
        dto.setUsuarioNombre(usuario.getNombre());
        dto.setUsuarioApellido(usuario.getApellido());
        dto.setUsuarioUsername(usuario.getUsername());
        
        return dto;
    }
    
    private UsuarioBusquedaDTO convertirAUsuarioBusquedaDTO(Usuario usuario, Long usuarioActualId) {
        UsuarioBusquedaDTO dto = new UsuarioBusquedaDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        
        // Determinar estado de relación
        EstadoRelacion estado = verificarEstadoRelacion(usuarioActualId, usuario.getId());
        dto.setEstadoAmistad(estado.getEstado());
        dto.setSolicitudEnviada(estado.isSolicitudEnviada());
        dto.setSolicitudRecibida(estado.isSolicitudRecibida());
        
        return dto;
    }
    
    // DTOs internos
    public static class EstadoRelacion {
        private String estado;
        private boolean solicitudEnviada;
        private boolean solicitudRecibida;
        
        public EstadoRelacion(String estado, boolean solicitudEnviada, boolean solicitudRecibida) {
            this.estado = estado;
            this.solicitudEnviada = solicitudEnviada;
            this.solicitudRecibida = solicitudRecibida;
        }
        
        // Getters
        public String getEstado() { return estado; }
        public boolean isSolicitudEnviada() { return solicitudEnviada; }
        public boolean isSolicitudRecibida() { return solicitudRecibida; }
    }
    
    public static class EstadisticasAmistad {
        private Long totalAmigos;
        private Long solicitudesRecibidas;
        private Long solicitudesEnviadas;
        
        public EstadisticasAmistad(Long totalAmigos, Long solicitudesRecibidas, Long solicitudesEnviadas) {
            this.totalAmigos = totalAmigos;
            this.solicitudesRecibidas = solicitudesRecibidas;
            this.solicitudesEnviadas = solicitudesEnviadas;
        }
        
        // Getters
        public Long getTotalAmigos() { return totalAmigos; }
        public Long getSolicitudesRecibidas() { return solicitudesRecibidas; }
        public Long getSolicitudesEnviadas() { return solicitudesEnviadas; }
    }
}