package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.*;
import com.sbs.open_app.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForoServicio {

    private final CategoriaForoRepositorio categoriaRepositorio;
    private final TemaForoRepositorio temaRepositorio;
    private final RespuestaForoRepositorio respuestaRepositorio;

    // ===== CATEGORÍAS =====

    @Transactional(readOnly = true)
    public List<CategoriaForo> obtenerTodasLasCategorias() {
        return categoriaRepositorio.findByActivaTrueOrderByOrdenAsc();
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaForo> obtenerCategoriaPorId(Long id) {
        return categoriaRepositorio.findById(id);
    }

    @Transactional
    public CategoriaForo crearCategoria(CategoriaForo categoria) {
        return categoriaRepositorio.save(categoria);
    }

    @Transactional
    public void inicializarCategoriasDefault() {
        if (categoriaRepositorio.count() == 0) {
            // Crear categorías basadas en las partes del Libro de Urantia
            crearCategoriaHelper("El Universo Central y los Superuniversos",
                "Discusiones sobre cosmología, la Trinidad, y el universo central",
                "🌌", "#3b82f6", 1);

            crearCategoriaHelper("El Universo Local",
                "Temas sobre nuestro universo local, Nebadón, y su creación",
                "⭐", "#60a5fa", 2);

            crearCategoriaHelper("Historia de Urantia",
                "Discusiones sobre la historia geológica y evolutiva de nuestro planeta",
                "🌍", "#1e3a8a", 3);

            crearCategoriaHelper("La Vida y Enseñanzas de Jesús",
                "Reflexiones sobre la cuarta parte del libro",
                "✝️", "#2563eb", 4);

            crearCategoriaHelper("Crecimiento Espiritual",
                "Compartir experiencias personales y reflexiones espirituales",
                "🙏", "#93c5fd", 5);

            crearCategoriaHelper("Organización y Eventos",
                "Anuncios, eventos, y organización de la comunidad",
                "📅", "#dbeafe", 6);

            crearCategoriaHelper("General",
                "Otros temas y conversaciones generales",
                "💬", "#94a3b8", 7);
        }
    }

    private void crearCategoriaHelper(String nombre, String descripcion, String icono, String color, int orden) {
        CategoriaForo categoria = new CategoriaForo();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setIcono(icono);
        categoria.setColor(color);
        categoria.setOrden(orden);
        categoria.setActiva(true);
        // fechaCreacion se establecerá automáticamente con el valor por defecto
        crearCategoria(categoria);
    }

    // ===== TEMAS =====

    @Transactional(readOnly = true)
    public List<TemaForo> obtenerTemasPorCategoria(Long categoriaId) {
        CategoriaForo categoria = categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return temaRepositorio.findByCategoriaAndActivoTrueOrderByFijadoDescFechaUltimaActividadDesc(categoria);
    }

    @Transactional(readOnly = true)
    public Page<TemaForo> obtenerTodosPaginado(Pageable pageable) {
        return temaRepositorio.findByActivoTrueOrderByFijadoDescFechaUltimaActividadDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<TemaForo> obtenerTemaPorId(Long id) {
        return temaRepositorio.findById(id);
    }

    @Transactional
    public TemaForo crearTema(TemaForo tema) {
        return temaRepositorio.save(tema);
    }

    @Transactional
    public TemaForo actualizarTema(TemaForo tema) {
        return temaRepositorio.save(tema);
    }

    @Transactional
    public void incrementarVistasTema(Long temaId) {
        Optional<TemaForo> temaOpt = temaRepositorio.findById(temaId);
        if (temaOpt.isPresent()) {
            TemaForo tema = temaOpt.get();
            tema.incrementarVistas();
            temaRepositorio.save(tema);
        }
    }

    @Transactional(readOnly = true)
    public List<TemaForo> buscarTemas(String query) {
        return temaRepositorio.buscarPorTitulo(query);
    }

    @Transactional
    public void eliminarTema(Long id) {
        Optional<TemaForo> temaOpt = temaRepositorio.findById(id);
        if (temaOpt.isPresent()) {
            TemaForo tema = temaOpt.get();
            tema.setActivo(false);
            temaRepositorio.save(tema);
        }
    }

    // ===== RESPUESTAS =====

    @Transactional(readOnly = true)
    public List<RespuestaForo> obtenerRespuestasPorTema(Long temaId) {
        TemaForo tema = temaRepositorio.findById(temaId)
                .orElseThrow(() -> new RuntimeException("Tema no encontrado"));
        return respuestaRepositorio.findByTemaAndActivoTrueOrderByFechaCreacionAsc(tema);
    }

    @Transactional
    public RespuestaForo crearRespuesta(RespuestaForo respuesta) {
        // Guardar la respuesta
        RespuestaForo nuevaRespuesta = respuestaRepositorio.save(respuesta);

        // Actualizar la última actividad del tema
        TemaForo tema = respuesta.getTema();
        tema.actualizarUltimaActividad();
        temaRepositorio.save(tema);

        return nuevaRespuesta;
    }

    @Transactional
    public RespuestaForo actualizarRespuesta(RespuestaForo respuesta) {
        respuesta.marcarComoEditado();
        return respuestaRepositorio.save(respuesta);
    }

    @Transactional
    public void eliminarRespuesta(Long id) {
        Optional<RespuestaForo> respuestaOpt = respuestaRepositorio.findById(id);
        if (respuestaOpt.isPresent()) {
            RespuestaForo respuesta = respuestaOpt.get();
            respuesta.setActivo(false);
            respuestaRepositorio.save(respuesta);
        }
    }

    // ===== ESTADÍSTICAS =====

    @Transactional(readOnly = true)
    public long contarTemasTotal() {
        return temaRepositorio.count();
    }

    @Transactional(readOnly = true)
    public long contarRespuestasTotal() {
        return respuestaRepositorio.count();
    }
}
