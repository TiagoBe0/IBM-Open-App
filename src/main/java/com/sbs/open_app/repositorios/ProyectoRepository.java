package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM Proyecto p LEFT JOIN FETCH p.tareas WHERE p.id = :id")
    Optional<Proyecto> findByIdWithTareas(@Param("id") Long id);

    @Query("SELECT p FROM Proyecto p WHERE p.usuario.id = :usuarioId AND p.estado = :estado")
    List<Proyecto> findByUsuarioIdAndEstado(@Param("usuarioId") Long usuarioId, @Param("estado") String estado);

    void deleteByIdAndUsuarioId(Long id, Long usuarioId);
}
