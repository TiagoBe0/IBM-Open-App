package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.ComentarioLibroDTO;
import com.sbs.open_app.entidades.ComentarioLibro;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.ComentarioLibroRepositorio;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
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
    private static final Pattern PATTERN_DOCUMENTO = Pattern.compile("(\\d+)-Documento(\\d{3})\\.html");

    /**
     * Lee el contenido del libro desde archivos HTML
     */
    public Map<String, Object> leerLibro() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            // Intentar cargar archivos HTML de la carpeta libro/
            Resource[] recursos = resolver.getResources("classpath:templates/libro/*-Documento*.html");

            if (recursos.length > 0) {
                log.info("📚 Encontrados {} archivos HTML del libro", recursos.length);
                return procesarArchivosHTML(recursos);
            } else {
                log.warn("⚠️ No se encontraron archivos HTML. Buscando libro.txt como fallback...");
                return leerLibroTexto();
            }
        } catch (Exception e) {
            log.error("❌ Error al cargar archivos HTML, intentando libro.txt", e);
            return leerLibroTexto();
        }
    }

    /**
     * Procesa archivos HTML del libro
     */
    private Map<String, Object> procesarArchivosHTML(Resource[] recursos) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> documentos = new ArrayList<>();

        // Ordenar recursos por número de documento
        Arrays.sort(recursos, (a, b) -> {
            try {
                int numA = extraerNumeroDocumento(a.getFilename());
                int numB = extraerNumeroDocumento(b.getFilename());
                return Integer.compare(numA, numB);
            } catch (Exception e) {
                return 0;
            }
        });

        for (Resource recurso : recursos) {
            try {
                String nombreArchivo = recurso.getFilename();
                int numeroDoc = extraerNumeroDocumento(nombreArchivo);

                log.info("📄 Procesando documento {}: {}", numeroDoc, nombreArchivo);

                Map<String, Object> documento = parsearDocumentoHTML(recurso, numeroDoc);
                if (documento != null) {
                    documentos.add(documento);
                }
            } catch (Exception e) {
                log.error("Error procesando archivo {}: {}", recurso.getFilename(), e.getMessage());
            }
        }

        resultado.put("documentos", documentos);
        resultado.put("totalDocumentos", documentos.size());

        log.info("✅ Libro cargado: {} documentos", documentos.size());
        return resultado;
    }

    /**
     * Parsea un documento HTML individual
     */
    private Map<String, Object> parsearDocumentoHTML(Resource recurso, int numeroDoc) throws IOException {
        try (InputStream is = recurso.getInputStream()) {
            Document doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), "");

            Map<String, Object> documento = new HashMap<>();
            documento.put("numero", numeroDoc);

            // Obtener título del documento
            String titulo = doc.title();
            if (titulo == null || titulo.isEmpty()) {
                Element h1 = doc.selectFirst("h1");
                titulo = h1 != null ? h1.text() : "Documento " + numeroDoc;
            }
            documento.put("titulo", titulo);

            // Obtener todas las secciones del documento
            List<Map<String, Object>> secciones = parsearSecciones(doc, numeroDoc);
            documento.put("secciones", secciones);

            return documento;
        }
    }

    /**
     * Parsea las secciones de un documento HTML
     */
    private List<Map<String, Object>> parsearSecciones(Document doc, int numeroDoc) {
        Map<Integer, Map<String, Object>> seccionesMap = new TreeMap<>();

        // Buscar todos los párrafos con clase "ctr" o "par" que contienen referencias
        Elements parrafos = doc.select("p.ctr, p.par, p");

        for (Element parrafo : parrafos) {
            // Buscar span con clase "pr" que contiene la referencia (D:S.P)
            Element spanRef = parrafo.selectFirst("span.pr, span.pnr");

            if (spanRef != null) {
                String refTexto = spanRef.text().trim();
                Matcher matcher = PATTERN_REFERENCIA.matcher(refTexto);

                if (matcher.find()) {
                    int docNumRef = Integer.parseInt(matcher.group(1));
                    int sec = Integer.parseInt(matcher.group(2));
                    int par = Integer.parseInt(matcher.group(3));

                    // Verificar que el documento coincide
                    if (docNumRef == numeroDoc) {
                        // Obtener o crear sección
                        Map<String, Object> seccion = seccionesMap.computeIfAbsent(sec, k -> {
                            Map<String, Object> nuevaSeccion = new HashMap<>();
                            nuevaSeccion.put("documento", docNumRef);
                            nuevaSeccion.put("seccion", sec);
                            nuevaSeccion.put("referencia", docNumRef + ":" + sec);
                            nuevaSeccion.put("parrafos", new ArrayList<String>());
                            return nuevaSeccion;
                        });

                        // Obtener el texto del párrafo (sin la referencia)
                        String textoParrafo = parrafo.text()
                                .replace(refTexto, "")
                                .trim();

                        if (!textoParrafo.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            List<String> parrafosLista = (List<String>) seccion.get("parrafos");
                            parrafosLista.add(textoParrafo);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(seccionesMap.values());
    }

    /**
     * Extrae el número de documento del nombre de archivo
     */
    private int extraerNumeroDocumento(String nombreArchivo) {
        if (nombreArchivo == null) return 0;

        Matcher matcher = PATTERN_DOCUMENTO.matcher(nombreArchivo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }

        // Fallback: buscar cualquier número de 3 dígitos
        Pattern fallback = Pattern.compile("(\\d{3})");
        matcher = fallback.matcher(nombreArchivo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

    /**
     * Método fallback para leer libro.txt (formato antiguo)
     */
    private Map<String, Object> leerLibroTexto() throws IOException {
        // Implementación anterior del método de lectura de TXT
        // (Mantener como fallback por si no hay archivos HTML)
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("documentos", new ArrayList<>());
        resultado.put("totalDocumentos", 0);
        resultado.put("error", "No se encontraron archivos HTML del libro. Por favor, copia los archivos HTML a src/main/resources/templates/libro/");
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
                if (secciones != null) {
                    for (Map<String, Object> seccion : secciones) {
                        if (seccion.get("seccion").equals(numeroSeccion)) {
                            return seccion;
                        }
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
