package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.ComentarioLibroDTO;
import com.sbs.open_app.entidades.ComentarioLibro;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.ComentarioLibroRepositorio;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LectorLibroService {

    @Autowired
    private ComentarioLibroRepositorio comentarioRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    private static final Pattern PATTERN_REFERENCIA = Pattern.compile("(\\d+):(\\d+)\\.(\\d+)");

    /**
     * Lee el contenido del libro desde el archivo libro.txt
     */
    public Map<String, Object> leerLibro() throws IOException {
        ClassPathResource resource = new ClassPathResource("libro.txt");
        List<String> lineas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                lineas.add(linea);
            }
        }

        return procesarContenidoLibro(lineas);
    }

    /**
     * Procesa el contenido del libro organizándolo por documentos y secciones
     */
    private Map<String, Object> procesarContenidoLibro(List<String> lineas) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> documentos = new ArrayList<>();

        Map<String, Object> documentoActual = null;
        Map<String, Object> seccionActual = null;
        List<Map<String, Object>> secciones = null;
        List<String> parrafos = null;

        Integer documentoNumeroActual = null;
        Integer seccionNumeroActual = null;

        for (String linea : lineas) {
            // Detectar inicio de documento (ej: "DOCUMENTO 0")
            if (linea.trim().matches("(?i)^DOCUMENTO\\s+\\d+.*")) {
                // Guardar sección anterior si existe
                if (seccionActual != null && parrafos != null && !parrafos.isEmpty()) {
                    seccionActual.put("parrafos", parrafos);
                    secciones.add(seccionActual);
                }

                // Guardar documento anterior si existe
                if (documentoActual != null) {
                    documentoActual.put("secciones", secciones);
                    documentos.add(documentoActual);
                }

                // Crear nuevo documento
                documentoActual = new HashMap<>();
                String[] partes = linea.trim().split("\\s+", 2);
                int numDoc = Integer.parseInt(partes[1].replaceAll("[^0-9]", ""));
                documentoActual.put("numero", numDoc);
                documentoActual.put("titulo", linea.trim());
                secciones = new ArrayList<>();
                seccionActual = null;
                parrafos = null;
                documentoNumeroActual = numDoc;
                seccionNumeroActual = null;
            }
            // Detectar referencia de párrafo (ej: "0:0.1")
            else if (PATTERN_REFERENCIA.matcher(linea.trim()).matches()) {
                Matcher matcher = PATTERN_REFERENCIA.matcher(linea.trim());
                if (matcher.find()) {
                    int doc = Integer.parseInt(matcher.group(1));
                    int sec = Integer.parseInt(matcher.group(2));
                    int par = Integer.parseInt(matcher.group(3));

                    // Verificar si cambió la sección (D:S)
                    if (seccionNumeroActual == null || seccionNumeroActual != sec) {
                        // Guardar sección anterior si existe
                        if (seccionActual != null && parrafos != null && !parrafos.isEmpty()) {
                            seccionActual.put("parrafos", parrafos);
                            secciones.add(seccionActual);
                        }

                        // Crear nueva sección
                        seccionActual = new HashMap<>();
                        seccionActual.put("documento", doc);
                        seccionActual.put("seccion", sec);
                        seccionActual.put("referencia", doc + ":" + sec);
                        parrafos = new ArrayList<>();
                        seccionNumeroActual = sec;
                    }
                    // Si es la misma sección, solo se agregará el texto al párrafo actual
                }
            }
            // Es contenido de párrafo
            else if (!linea.trim().isEmpty() && parrafos != null) {
                parrafos.add(linea.trim());
            }
        }

        // Agregar la última sección y documento
        if (seccionActual != null && parrafos != null && !parrafos.isEmpty()) {
            seccionActual.put("parrafos", parrafos);
            secciones.add(seccionActual);
        }
        if (documentoActual != null) {
            documentoActual.put("secciones", secciones);
            documentos.add(documentoActual);
        }

        resultado.put("documentos", documentos);
        resultado.put("totalDocumentos", documentos.size());
        return resultado;
    }

    /**
     * Obtiene una sección específica del libro
     */
    public Map<String, Object> obtenerSeccion(Integer numeroDocumento, Integer numeroSeccion) throws IOException {
        Map<String, Object> libro = leerLibro();
        List<Map<String, Object>> documentos = (List<Map<String, Object>>) libro.get("documentos");

        for (Map<String, Object> doc : documentos) {
            if (doc.get("numero").equals(numeroDocumento)) {
                List<Map<String, Object>> secciones = (List<Map<String, Object>>) doc.get("secciones");
                for (Map<String, Object> seccion : secciones) {
                    if (seccion.get("seccion").equals(numeroSeccion)) {
                        return seccion;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Guarda un comentario
     */
    @Transactional
    public ComentarioLibroDTO guardarComentario(Long usuarioId, ComentarioLibroDTO dto) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ComentarioLibro comentario = new ComentarioLibro();
        comentario.setUsuario(usuario);
        comentario.setNumeroDocumento(dto.getNumeroDocumento());
        comentario.setNumeroSeccion(dto.getNumeroSeccion());
        comentario.setNumeroParrafo(dto.getNumeroParrafo());
        comentario.setContenido(dto.getContenido());
        comentario.setEsPublico(dto.getEsPublico() != null ? dto.getEsPublico() : true);

        ComentarioLibro guardado = comentarioRepositorio.save(comentario);
        return convertirADTO(guardado);
    }

    /**
     * Obtiene comentarios de una sección
     */
    public List<ComentarioLibroDTO> obtenerComentariosSeccion(Integer numeroDocumento, Integer numeroSeccion) {
        List<ComentarioLibro> comentarios = comentarioRepositorio.findComentariosPorSeccion(numeroDocumento, numeroSeccion);
        return comentarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene comentarios de un párrafo específico
     */
    public List<ComentarioLibroDTO> obtenerComentariosParrafo(Integer numeroDocumento, Integer numeroSeccion, Integer numeroParrafo) {
        List<ComentarioLibro> comentarios = comentarioRepositorio
                .findByNumeroDocumentoAndNumeroSeccionAndNumeroParrafoAndEsPublicoTrue(
                        numeroDocumento, numeroSeccion, numeroParrafo);
        return comentarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene comentarios de un usuario
     */
    public List<ComentarioLibroDTO> obtenerComentariosUsuario(Long usuarioId) {
        List<ComentarioLibro> comentarios = comentarioRepositorio.findByUsuarioId(usuarioId);
        return comentarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un comentario
     */
    @Transactional
    public void eliminarComentario(Long comentarioId, Long usuarioId) {
        ComentarioLibro comentario = comentarioRepositorio.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comentario.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }

        comentarioRepositorio.delete(comentario);
    }

    /**
     * Actualiza un comentario
     */
    @Transactional
    public ComentarioLibroDTO actualizarComentario(Long comentarioId, Long usuarioId, String nuevoContenido) {
        ComentarioLibro comentario = comentarioRepositorio.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comentario.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para editar este comentario");
        }

        comentario.setContenido(nuevoContenido);
        ComentarioLibro actualizado = comentarioRepositorio.save(comentario);
        return convertirADTO(actualizado);
    }

    /**
     * Convierte una entidad a DTO
     */
    private ComentarioLibroDTO convertirADTO(ComentarioLibro comentario) {
        ComentarioLibroDTO dto = new ComentarioLibroDTO();
        dto.setId(comentario.getId());
        dto.setUsuarioId(comentario.getUsuario().getId());
        dto.setUsuarioNombre(comentario.getUsuario().getNombre() + " " + comentario.getUsuario().getApellido());
        dto.setUsuarioUsername(comentario.getUsuario().getUsername());
        dto.setNumeroDocumento(comentario.getNumeroDocumento());
        dto.setNumeroSeccion(comentario.getNumeroSeccion());
        dto.setNumeroParrafo(comentario.getNumeroParrafo());
        dto.setContenido(comentario.getContenido());
        dto.setFechaCreacion(comentario.getFechaCreacion());
        dto.setFechaModificacion(comentario.getFechaModificacion());
        dto.setEsPublico(comentario.getEsPublico());
        return dto;
    }
}
