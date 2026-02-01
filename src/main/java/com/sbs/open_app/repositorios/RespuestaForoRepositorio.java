package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.RespuestaForo;
import com.sbs.open_app.entidades.TemaForo;
import com.sbs.open_app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RespuestaForoRepositorio extends JpaRepository<RespuestaForo, Long> {

    // Encontrar respuestas por tema
    List<RespuestaForo> findByTemaAndActivoTrueOrderByFechaCreacionAsc(TemaForo tema);

    // Encontrar respuestas por autor
    List<RespuestaForo> findByAutorAndActivoTrueOrderByFechaCreacionDesc(Usuario autor);

    // Contar respuestas por tema
    long countByTemaAndActivoTrue(TemaForo tema);
}
