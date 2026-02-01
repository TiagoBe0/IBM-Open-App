package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.ComentarioLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioLibroRepositorio extends JpaRepository<ComentarioLibro, Long> {

    List<ComentarioLibro> findByNumeroDocumentoAndNumeroSeccionAndNumeroParrafoAndEsPublicoTrue(
            Integer numeroDocumento, Integer numeroSeccion, Integer numeroParrafo);

    List<ComentarioLibro> findByUsuarioIdAndNumeroDocumentoAndNumeroSeccion(
            Long usuarioId, Integer numeroDocumento, Integer numeroSeccion);

    List<ComentarioLibro> findByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM ComentarioLibro c WHERE c.numeroDocumento = :doc AND c.numeroSeccion = :sec AND c.esPublico = true ORDER BY c.numeroParrafo, c.fechaCreacion")
    List<ComentarioLibro> findComentariosPorSeccion(@Param("doc") Integer numeroDocumento, @Param("sec") Integer numeroSeccion);

    @Query("SELECT COUNT(c) FROM ComentarioLibro c WHERE c.numeroDocumento = :doc AND c.numeroSeccion = :sec AND c.numeroParrafo = :par")
    Long contarComentariosPorParrafo(@Param("doc") Integer numeroDocumento, @Param("sec") Integer numeroSeccion, @Param("par") Integer numeroParrafo);
}
