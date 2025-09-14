package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    
    // Obtener conversación entre dos usuarios con paginación
    @Query("SELECT m FROM Mensaje m WHERE " +
           "((m.remitente.id = :usuario1 AND m.destinatario.id = :usuario2) OR " +
           "(m.remitente.id = :usuario2 AND m.destinatario.id = :usuario1)) " +
           "AND ((m.remitente.id = :usuarioActual AND m.eliminadoRemitente = false) OR " +
           "(m.destinatario.id = :usuarioActual AND m.eliminadoDestinatario = false)) " +
           "ORDER BY m.fechaEnvio DESC")
    Page<Mensaje> findConversationBetweenUsers(@Param("usuario1") Long usuario1,
                                               @Param("usuario2") Long usuario2,
                                               @Param("usuarioActual") Long usuarioActual,
                                               Pageable pageable);
    
    // Obtener último mensaje entre dos usuarios
    @Query("SELECT m FROM Mensaje m WHERE " +
           "((m.remitente.id = :usuario1 AND m.destinatario.id = :usuario2) OR " +
           "(m.remitente.id = :usuario2 AND m.destinatario.id = :usuario1)) " +
           "AND ((m.remitente.id = :usuarioActual AND m.eliminadoRemitente = false) OR " +
           "(m.destinatario.id = :usuarioActual AND m.eliminadoDestinatario = false)) " +
           "ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findLastMessageBetweenUsers(@Param("usuario1") Long usuario1,
                                             @Param("usuario2") Long usuario2,
                                             @Param("usuarioActual") Long usuarioActual);
    
    // Contar mensajes no leídos para un usuario
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE " +
           "m.destinatario.id = :usuarioId " +
           "AND m.leido = false " +
           "AND m.eliminadoDestinatario = false")
    Long countUnreadMessages(@Param("usuarioId") Long usuarioId);
    
    // Contar mensajes no leídos de un remitente específico
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE " +
           "m.destinatario.id = :destinatarioId " +
           "AND m.remitente.id = :remitenteId " +
           "AND m.leido = false " +
           "AND m.eliminadoDestinatario = false")
    Long countUnreadMessagesFromUser(@Param("destinatarioId") Long destinatarioId,
                                    @Param("remitenteId") Long remitenteId);
    
    // Marcar mensajes como leídos
    @Query("UPDATE Mensaje m SET m.leido = true, m.fechaLectura = :fechaLectura " +
           "WHERE m.destinatario.id = :destinatarioId " +
           "AND m.remitente.id = :remitenteId " +
           "AND m.leido = false")
    void markMessagesAsRead(@Param("destinatarioId") Long destinatarioId,
                           @Param("remitenteId") Long remitenteId,
                           @Param("fechaLectura") LocalDateTime fechaLectura);
    
    // Obtener usuarios con los que se ha intercambiado mensajes
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.remitente.id = :usuarioId THEN m.destinatario ELSE m.remitente END " +
           "FROM Mensaje m WHERE " +
           "(m.remitente.id = :usuarioId OR m.destinatario.id = :usuarioId) " +
           "AND ((m.remitente.id = :usuarioId AND m.eliminadoRemitente = false) OR " +
           "(m.destinatario.id = :usuarioId AND m.eliminadoDestinatario = false))")
    List<com.sbs.open_app.entidades.Usuario> findUsersInConversation(@Param("usuarioId") Long usuarioId);
    
    // Obtener mensajes enviados por un usuario
    @Query("SELECT m FROM Mensaje m WHERE " +
           "m.remitente.id = :usuarioId " +
           "AND m.eliminadoRemitente = false " +
           "ORDER BY m.fechaEnvio DESC")
    Page<Mensaje> findMessagesSentByUser(@Param("usuarioId") Long usuarioId, Pageable pageable);
    
    // Obtener mensajes recibidos por un usuario
    @Query("SELECT m FROM Mensaje m WHERE " +
           "m.destinatario.id = :usuarioId " +
           "AND m.eliminadoDestinatario = false " +
           "ORDER BY m.fechaEnvio DESC")
    Page<Mensaje> findMessagesReceivedByUser(@Param("usuarioId") Long usuarioId, Pageable pageable);
    
    // Eliminar mensajes antiguos (para mantenimiento)
    @Query("DELETE FROM Mensaje m WHERE " +
           "m.fechaEnvio < :fechaLimite " +
           "AND m.eliminadoRemitente = true " +
           "AND m.eliminadoDestinatario = true")
    void deleteOldMessages(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Buscar mensajes por contenido
    @Query("SELECT m FROM Mensaje m WHERE " +
           "(m.remitente.id = :usuarioId OR m.destinatario.id = :usuarioId) " +
           "AND LOWER(m.contenido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "AND ((m.remitente.id = :usuarioId AND m.eliminadoRemitente = false) OR " +
           "(m.destinatario.id = :usuarioId AND m.eliminadoDestinatario = false)) " +
           "ORDER BY m.fechaEnvio DESC")
    Page<Mensaje> searchMessages(@Param("usuarioId") Long usuarioId,
                                @Param("busqueda") String busqueda,
                                Pageable pageable);
}