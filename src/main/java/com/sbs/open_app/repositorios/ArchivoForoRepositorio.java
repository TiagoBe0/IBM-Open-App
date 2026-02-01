package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.ArchivoForo;
import com.sbs.open_app.entidades.TemaForo;
import com.sbs.open_app.entidades.RespuestaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArchivoForoRepositorio extends JpaRepository<ArchivoForo, Long> {

    // Encontrar archivos por tema
    List<ArchivoForo> findByTemaOrderByFechaSubidaAsc(TemaForo tema);

    // Encontrar archivos por respuesta
    List<ArchivoForo> findByRespuestaOrderByFechaSubidaAsc(RespuestaForo respuesta);

    // Contar archivos por tema
    long countByTema(TemaForo tema);

    // Contar archivos por respuesta
    long countByRespuesta(RespuestaForo respuesta);
}
