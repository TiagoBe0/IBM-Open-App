package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.TemaForo;
import com.sbs.open_app.entidades.CategoriaForo;
import com.sbs.open_app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface TemaForoRepositorio extends JpaRepository<TemaForo, Long> {

    // Encontrar temas por categoría
    List<TemaForo> findByCategoriaAndActivoTrueOrderByFijadoDescFechaUltimaActividadDesc(CategoriaForo categoria);

    // Encontrar temas por autor
    List<TemaForo> findByAutorAndActivoTrueOrderByFechaCreacionDesc(Usuario autor);

    // Buscar temas (paginado)
    Page<TemaForo> findByActivoTrueOrderByFijadoDescFechaUltimaActividadDesc(Pageable pageable);

    // Buscar por categoría (paginado)
    Page<TemaForo> findByCategoriaAndActivoTrueOrderByFijadoDescFechaUltimaActividadDesc(CategoriaForo categoria, Pageable pageable);

    // Buscar por título
    @Query("SELECT t FROM TemaForo t WHERE t.activo = true AND LOWER(t.titulo) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY t.fechaUltimaActividad DESC")
    List<TemaForo> buscarPorTitulo(@Param("query") String query);

    // Contar temas por categoría
    long countByCategoriaAndActivoTrue(CategoriaForo categoria);
}
