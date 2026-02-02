package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByProyectoId(Long proyectoId);

    @Query("SELECT t FROM Tarea t WHERE t.proyecto.id = :proyectoId AND t.completada = :completada")
    List<Tarea> findByProyectoIdAndCompletada(@Param("proyectoId") Long proyectoId, @Param("completada") boolean completada);

    @Query("SELECT t FROM Tarea t WHERE t.proyecto.id = :proyectoId AND t.estado = :estado")
    List<Tarea> findByProyectoIdAndEstado(@Param("proyectoId") Long proyectoId, @Param("estado") String estado);

    void deleteByIdAndProyectoId(Long id, Long proyectoId);
}
