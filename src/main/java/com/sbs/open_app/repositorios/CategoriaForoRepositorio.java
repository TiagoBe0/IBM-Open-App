package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.CategoriaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaForoRepositorio extends JpaRepository<CategoriaForo, Long> {

    // Encontrar categorías activas ordenadas por orden
    List<CategoriaForo> findByActivaTrueOrderByOrdenAsc();

    // Buscar por nombre
    CategoriaForo findByNombre(String nombre);
}
