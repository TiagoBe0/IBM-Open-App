package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.MensajeDTO;
import com.sbs.open_app.dto.ConversacionDTO;
import com.sbs.open_app.dto.EnviarMensajeDTO;
import com.sbs.open_app.entidades.EstadisticasMensajes;
import com.sbs.open_app.entidades.Mensaje;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.MensajeRepository;
import com.sbs.open_app.repositorios.AmistadRepository;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MensajeService {
    
    private static final Logger logger = LoggerFactory.getLogger(MensajeService.class);
    
    private final MensajeRepository mensajeRepository;
    private final AmistadRepository amistadRepository;
    private final UsuarioRepositorio usuarioRepository;
    
    /**
     * Enviar mensaje a un amigo
     */
    public MensajeDTO enviarMensaje(Long remitenteId, EnviarMensajeDTO mensajeDTO) {
        logger.info("Enviando mensaje de {} a {}", remitenteId, mensajeDTO.getDestinatarioId());
        
        // Validaciones básicas
        if (remitenteId.equals(mensajeDTO.getDestinatarioId())) {
            throw new IllegalArgumentException("No puedes enviarte mensajes a ti mismo");
        }
        
        // Verificar que son amigos
        if (!amistadRepository.areFreinds(remitenteId, mensajeDTO.getDestinatarioId())) {
            throw new IllegalArgumentException("Solo puedes enviar mensajes a tus amigos");
        }
        
        // Verificar que los usuarios existen
        Usuario remitente = usuarioRepository.findById(remitenteId)
            .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));
        Usuario destinatario = usuarioRepository.findById(mensajeDTO.getDestinatarioId())
            .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));
        
        // Validar contenido
        if (mensajeDTO.getContenido() == null || mensajeDTO.getContenido().trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        if (mensajeDTO.getContenido().length() > 2000) {
            throw new IllegalArgumentException("El mensaje es demasiado largo (máximo 2000 caracteres)");
        }
        
        // Crear mensaje
        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setContenido(mensajeDTO.getContenido().trim());
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setTipoMensaje(Mensaje.TipoMensaje.valueOf(
            mensajeDTO.getTipoMensaje() != null ? mensajeDTO.getTipoMensaje() : "TEXTO"
        ));
        mensaje.setArchivoAdjunto(mensajeDTO.getArchivoAdjunto());
        
        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);
        logger.info("Mensaje enviado exitosamente con ID: {}", mensajeGuardado.getId());
        
        return convertirADTO(mensajeGuardado, remitenteId);
    }
    
    /**
     * Obtener conversación con paginación
     */
    @Transactional(readOnly = true)
    public List<MensajeDTO> obtenerConversacion(Long usuarioId, Long amigoId, int pagina, int tamaño) {
        logger.info("Obteniendo conversación entre {} y {}", usuarioId, amigoId);
        
        // Verificar que son amigos
        if (!amistadRepository.areFreinds(usuarioId, amigoId)) {
            throw new IllegalArgumentException("Solo puedes ver mensajes con tus amigos");
        }
        
        Pageable pageable = PageRequest.of(pagina, tamaño);
        Page<Mensaje> mensajes = mensajeRepository.findConversationBetweenUsers(
            usuarioId, amigoId, usuarioId, pageable
        );
        
        return mensajes.getContent().stream()
            .map(mensaje -> convertirADTO(mensaje, usuarioId))
            .collect(Collectors.toList());
    }
    
    /**
     * Marcar mensajes como leídos
     */
    public void marcarComoLeidos(Long destinatarioId, Long remitenteId) {
        logger.info("Marcando mensajes como leídos: destinatario={}, remitente={}", destinatarioId, remitenteId);
        
        mensajeRepository.markMessagesAsRead(destinatarioId, remitenteId, LocalDateTime.now());
    }
    
    /**
     * Obtener lista de conversaciones
     */
    @Transactional(readOnly = true)
    public List<ConversacionDTO> obtenerConversaciones(Long usuarioId) {
        logger.info("Obteniendo conversaciones del usuario: {}", usuarioId);
        
        List<Usuario> usuariosConversacion = mensajeRepository.findUsersInConversation(usuarioId);
        
        return usuariosConversacion.stream()
            .filter(usuario -> amistadRepository.areFreinds(usuarioId, usuario.getId()))
            .map(amigo -> {
                // Obtener último mensaje
                List<Mensaje> ultimosMensajes = mensajeRepository.findLastMessageBetweenUsers(
                    usuarioId, amigo.getId(), usuarioId
                );
                
                MensajeDTO ultimoMensaje = ultimosMensajes.isEmpty() ? null :
                    convertirADTO(ultimosMensajes.get(0), usuarioId);
                
                // Contar mensajes no leídos
                Long noLeidos = mensajeRepository.countUnreadMessagesFromUser(usuarioId, amigo.getId());
                
                ConversacionDTO conversacion = new ConversacionDTO();
                conversacion.setAmigoId(amigo.getId());
                conversacion.setAmigoNombre(amigo.getNombre());
                conversacion.setAmigoApellido(amigo.getApellido());
                conversacion.setAmigoEmail(amigo.getEmail());
                conversacion.setAmigoFotoPerfil(amigo.getFotoPerfil());
                conversacion.setUltimoMensaje(ultimoMensaje);
                conversacion.setMensajesNoLeidos(noLeidos);
                conversacion.setUltimaActividad(
                    ultimoMensaje != null ? ultimoMensaje.getFechaEnvio() : null
                );
                
                return conversacion;
            })
            .sorted((c1, c2) -> {
                LocalDateTime fecha1 = c1.getUltimaActividad();
                LocalDateTime fecha2 = c2.getUltimaActividad();
                if (fecha1 == null && fecha2 == null) return 0;
                if (fecha1 == null) return 1;
                if (fecha2 == null) return -1;
                return fecha2.compareTo(fecha1);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Eliminar mensaje para un usuario
     */
    public void eliminarMensaje(Long usuarioId, Long mensajeId) {
        logger.info("Eliminando mensaje {} para usuario {}", mensajeId, usuarioId);
        
        Mensaje mensaje = mensajeRepository.findById(mensajeId)
            .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar permisos
        if (!mensaje.getRemitente().getId().equals(usuarioId) && 
            !mensaje.getDestinatario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar este mensaje");
        }
        
        // Marcar como eliminado según el usuario
        if (mensaje.getRemitente().getId().equals(usuarioId)) {
            mensaje.setEliminadoRemitente(true);
        } else {
            mensaje.setEliminadoDestinatario(true);
        }
        
        mensajeRepository.save(mensaje);
        
        // Si ambos eliminaron el mensaje, eliminarlo físicamente
        if (mensaje.getEliminadoRemitente() && mensaje.getEliminadoDestinatario()) {
            mensajeRepository.delete(mensaje);
        }
    }
    
    /**
     * Obtener estadísticas de mensajes
     */
    @Transactional(readOnly = true)
    public EstadisticasMensajes obtenerEstadisticas(Long usuarioId) {
        logger.info("Obteniendo estadísticas de mensajes para usuario: {}", usuarioId);
        
        // Contar mensajes enviados
        Page<Mensaje> enviados = mensajeRepository.findMessagesSentByUser(
            usuarioId, PageRequest.of(0, 1)
        );
        
        // Contar mensajes recibidos
        Page<Mensaje> recibidos = mensajeRepository.findMessagesReceivedByUser(
            usuarioId, PageRequest.of(0, 1)
        );
        
        // Mensajes no leídos
        Long noLeidos = mensajeRepository.countUnreadMessages(usuarioId);
        
        // Conversaciones activas
        Long conversacionesActivas = (long) mensajeRepository.findUsersInConversation(usuarioId).size();
        
        return new EstadisticasMensajes(
            enviados.getTotalElements(),
            recibidos.getTotalElements(),
            noLeidos,
            conversacionesActivas
        );
    }
    
    /**
     * Buscar mensajes
     */
    @Transactional(readOnly = true)
    public List<MensajeDTO> buscarMensajes(Long usuarioId, String busqueda, int pagina, int tamaño) {
        logger.info("Buscando mensajes para usuario {}: {}", usuarioId, busqueda);
        
        if (busqueda == null || busqueda.trim().length() < 2) {
            throw new IllegalArgumentException("El término de búsqueda debe tener al menos 2 caracteres");
        }
        
        Pageable pageable = PageRequest.of(pagina, tamaño);
        Page<Mensaje> mensajes = mensajeRepository.searchMessages(usuarioId, busqueda.trim(), pageable);
        
        return mensajes.getContent().stream()
            .map(mensaje -> convertirADTO(mensaje, usuarioId))
            .collect(Collectors.toList());
    }
    
    // Método de conversión
    private MensajeDTO convertirADTO(Mensaje mensaje, Long usuarioActualId) {
        MensajeDTO dto = new MensajeDTO();
        dto.setId(mensaje.getId());
        dto.setRemitenteId(mensaje.getRemitente().getId());
        dto.setDestinatarioId(mensaje.getDestinatario().getId());
        dto.setContenido(mensaje.getContenido());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        dto.setFechaLectura(mensaje.getFechaLectura());
        dto.setLeido(mensaje.getLeido());
        dto.setTipoMensaje(mensaje.getTipoMensaje().name());
        dto.setArchivoAdjunto(mensaje.getArchivoAdjunto());
        
        // Información del remitente
        Usuario remitente = mensaje.getRemitente();
        dto.setRemitenteNombre(remitente.getNombre());
        dto.setRemitenteApellido(remitente.getApellido());
        dto.setRemitenteEmail(remitente.getEmail());
        dto.setRemitenteFotoPerfil(remitente.getFotoPerfil());
        
        // Información del destinatario
        Usuario destinatario = mensaje.getDestinatario();
        dto.setDestinatarioNombre(destinatario.getNombre());
        dto.setDestinatarioApellido(destinatario.getApellido());
        dto.setDestinatarioEmail(destinatario.getEmail());
        dto.setDestinatarioFotoPerfil(destinatario.getFotoPerfil());
        
        // Metadatos
        dto.setEsPropio(mensaje.getRemitente().getId().equals(usuarioActualId));
        dto.setMinutosTranscurridos(ChronoUnit.MINUTES.between(mensaje.getFechaEnvio(), LocalDateTime.now()));
        
        return dto;
    }
}