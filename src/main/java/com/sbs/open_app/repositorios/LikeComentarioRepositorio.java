package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.ComentarioLibro;
import com.sbs.open_app.entidades.LikeComentario;
import com.sbs.open_app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar los likes (velas) en los comentarios del libro
 */
@Repository
public interface LikeComentarioRepositorio extends JpaRepository<LikeComentario, Long> {

    /**
     * Cuenta el número de likes (velas) de un comentario
     */
    long countByComentario(ComentarioLibro comentario);

    /**
     * Cuenta el número de likes (velas) de un comentario por ID
     */
    @Query("SELECT COUNT(l) FROM LikeComentario l WHERE l.comentario.id = :comentarioId")
    long countByComentarioId(@Param("comentarioId") Long comentarioId);

    /**
     * Verifica si un usuario ya dio like a un comentario
     */
    boolean existsByComentarioAndUsuario(ComentarioLibro comentario, Usuario usuario);

    /**
     * Verifica si un usuario ya dio like a un comentario por IDs
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LikeComentario l WHERE l.comentario.id = :comentarioId AND l.usuario.id = :usuarioId")
    boolean existsByComentarioIdAndUsuarioId(@Param("comentarioId") Long comentarioId, @Param("usuarioId") Long usuarioId);

    /**
     * Encuentra un like específico por comentario y usuario
     */
    Optional<LikeComentario> findByComentarioAndUsuario(ComentarioLibro comentario, Usuario usuario);

    /**
     * Encuentra un like específico por IDs
     */
    @Query("SELECT l FROM LikeComentario l WHERE l.comentario.id = :comentarioId AND l.usuario.id = :usuarioId")
    Optional<LikeComentario> findByComentarioIdAndUsuarioId(@Param("comentarioId") Long comentarioId, @Param("usuarioId") Long usuarioId);
}
