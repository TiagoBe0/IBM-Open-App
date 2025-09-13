package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Obtener mensajes por sala de chat ordenados por fecha
    @Query("SELECT c FROM ChatMessage c WHERE c.salaChat = :salaChat ORDER BY c.fechaEnvio DESC")
    List<ChatMessage> findBySalaChatOrderByFechaEnvioDesc(@Param("salaChat") String salaChat);
    
    // Obtener últimos N mensajes de una sala
    @Query("SELECT c FROM ChatMessage c WHERE c.salaChat = :salaChat ORDER BY c.fechaEnvio DESC LIMIT :limite")
    List<ChatMessage> findUltimosMensajes(@Param("salaChat") String salaChat, @Param("limite") int limite);
    
    // Obtener mensajes desde una fecha específica
    @Query("SELECT c FROM ChatMessage c WHERE c.salaChat = :salaChat AND c.fechaEnvio >= :fechaDesde ORDER BY c.fechaEnvio ASC")
    List<ChatMessage> findMensajesDesde(@Param("salaChat") String salaChat, @Param("fechaDesde") LocalDateTime fechaDesde);
    
    // Obtener mensajes de un usuario específico
    List<ChatMessage> findByUsuarioIdOrderByFechaEnvioDesc(Long usuarioId);
    
    // Contar mensajes de una sala
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.salaChat = :salaChat")
    Long contarMensajesPorSala(@Param("salaChat") String salaChat);
    
    // Buscar mensajes que contengan texto específico
    @Query("SELECT c FROM ChatMessage c WHERE c.salaChat = :salaChat AND LOWER(c.contenido) LIKE LOWER(CONCAT('%', :texto, '%')) ORDER BY c.fechaEnvio DESC")
    List<ChatMessage> buscarMensajesPorTexto(@Param("salaChat") String salaChat, @Param("texto") String texto);
}