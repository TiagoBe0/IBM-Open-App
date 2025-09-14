package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Amistad;
import com.sbs.open_app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, Long> {
    
    // Buscar solicitud específica entre dos usuarios
    @Query("SELECT a FROM Amistad a WHERE " +
           "(a.usuario.id = :usuarioId AND a.amigo.id = :amigoId) OR " +
           "(a.usuario.id = :amigoId AND a.amigo.id = :usuarioId)")
    Optional<Amistad> findRelationBetweenUsers(@Param("usuarioId") Long usuarioId, 
                                               @Param("amigoId") Long amigoId);
    
    // Obtener todos los amigos confirmados de un usuario
    @Query("SELECT a FROM Amistad a WHERE " +
           "(a.usuario.id = :usuarioId OR a.amigo.id = :usuarioId) " +
           "AND a.estado = 'ACEPTADA'")
    List<Amistad> findActiveFreindsByUser(@Param("usuarioId") Long usuarioId);
    
    // Obtener solicitudes pendientes RECIBIDAS por un usuario
    @Query("SELECT a FROM Amistad a WHERE a.amigo.id = :usuarioId AND a.estado = 'PENDIENTE'")
    List<Amistad> findPendingRequestsReceived(@Param("usuarioId") Long usuarioId);
    
    // Obtener solicitudes pendientes ENVIADAS por un usuario
    @Query("SELECT a FROM Amistad a WHERE a.usuario.id = :usuarioId AND a.estado = 'PENDIENTE'")
    List<Amistad> findPendingRequestsSent(@Param("usuarioId") Long usuarioId);
    
    // Obtener solicitudes rechazadas de un usuario
    @Query("SELECT a FROM Amistad a WHERE " +
           "(a.usuario.id = :usuarioId OR a.amigo.id = :usuarioId) " +
           "AND a.estado = 'RECHAZADA'")
    List<Amistad> findRejectedRequestsByUser(@Param("usuarioId") Long usuarioId);
    
    // Contar amigos activos de un usuario
    @Query("SELECT COUNT(a) FROM Amistad a WHERE " +
           "(a.usuario.id = :usuarioId OR a.amigo.id = :usuarioId) " +
           "AND a.estado = 'ACEPTADA'")
    Long countActiveFreindsByUser(@Param("usuarioId") Long usuarioId);
    
    // Verificar si dos usuarios son amigos
    @Query("SELECT COUNT(a) > 0 FROM Amistad a WHERE " +
           "(a.usuario.id = :usuarioId AND a.amigo.id = :amigoId) OR " +
           "(a.usuario.id = :amigoId AND a.amigo.id = :usuarioId) " +
           "AND a.estado = 'ACEPTADA'")
    boolean areFreinds(@Param("usuarioId") Long usuarioId, @Param("amigoId") Long amigoId);
    
    // Verificar si existe solicitud pendiente entre usuarios
    @Query("SELECT COUNT(a) > 0 FROM Amistad a WHERE " +
           "((a.usuario.id = :usuarioId AND a.amigo.id = :amigoId) OR " +
           "(a.usuario.id = :amigoId AND a.amigo.id = :usuarioId)) " +
           "AND a.estado = 'PENDIENTE'")
    boolean hasPendingRequest(@Param("usuarioId") Long usuarioId, @Param("amigoId") Long amigoId);
    
    // Buscar usuarios por nombre/email para agregar como amigos - CORREGIDO
    @Query("SELECT u FROM Usuario u WHERE " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "AND u.id != :usuarioId " +
           "AND u.activo = true")
    List<Usuario> findPotentialFreinds(@Param("busqueda") String busqueda, 
                                      @Param("usuarioId") Long usuarioId);
}