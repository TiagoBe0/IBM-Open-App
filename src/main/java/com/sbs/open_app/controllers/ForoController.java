package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.*;
import com.sbs.open_app.servicios.ForoServicio;
import com.sbs.open_app.servicios.UsuarioServicio;
import com.sbs.open_app.servicios.ArchivoForoServicio;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/foro")
@RequiredArgsConstructor
public class ForoController {

    private static final Logger logger = LoggerFactory.getLogger(ForoController.class);

    private final ForoServicio foroServicio;
    private final UsuarioServicio usuarioServicio;
    private final ArchivoForoServicio archivoServicio;

    // Obtener usuario autenticado
    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioServicio.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ===== VISTAS =====

    // Página principal del foro - Lista de categorías
    @GetMapping
    public String verForo(Model model) {
        List<CategoriaForo> categorias = foroServicio.obtenerTodasLasCategorias();
        long totalTemas = foroServicio.contarTemasTotal();
        long totalRespuestas = foroServicio.contarRespuestasTotal();

        model.addAttribute("categorias", categorias);
        model.addAttribute("totalTemas", totalTemas);
        model.addAttribute("totalRespuestas", totalRespuestas);
        model.addAttribute("usuario", getUsuarioAutenticado());

        return "foro/index";
    }

    // Ver temas de una categoría
    @GetMapping("/categoria/{id}")
    public String verCategoria(@PathVariable Long id, Model model) {
        Optional<CategoriaForo> categoriaOpt = foroServicio.obtenerCategoriaPorId(id);
        if (categoriaOpt.isEmpty()) {
            return "redirect:/foro";
        }

        CategoriaForo categoria = categoriaOpt.get();
        List<TemaForo> temas = foroServicio.obtenerTemasPorCategoria(id);

        model.addAttribute("categoria", categoria);
        model.addAttribute("temas", temas);
        model.addAttribute("usuario", getUsuarioAutenticado());

        return "foro/categoria";
    }

    // Ver tema y sus respuestas
    @GetMapping("/tema/{id}")
    public String verTema(@PathVariable Long id, Model model) {
        Optional<TemaForo> temaOpt = foroServicio.obtenerTemaPorId(id);
        if (temaOpt.isEmpty()) {
            return "redirect:/foro";
        }

        TemaForo tema = temaOpt.get();
        List<RespuestaForo> respuestas = foroServicio.obtenerRespuestasPorTema(id);

        // Incrementar vistas
        foroServicio.incrementarVistasTema(id);

        model.addAttribute("tema", tema);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("usuario", getUsuarioAutenticado());

        return "foro/tema";
    }

    // Formulario para crear nuevo tema
    @GetMapping("/nuevo-tema")
    public String formularioNuevoTema(@RequestParam(required = false) Long categoriaId, Model model) {
        List<CategoriaForo> categorias = foroServicio.obtenerTodasLasCategorias();

        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriaIdSeleccionada", categoriaId);
        model.addAttribute("usuario", getUsuarioAutenticado());

        return "foro/nuevo-tema";
    }

    // ===== ACCIONES =====

    // Crear nuevo tema
    @PostMapping("/crear-tema")
    public String crearTema(
            @RequestParam String titulo,
            @RequestParam String contenido,
            @RequestParam Long categoriaId,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = getUsuarioAutenticado();
            Optional<CategoriaForo> categoriaOpt = foroServicio.obtenerCategoriaPorId(categoriaId);

            if (categoriaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/foro/nuevo-tema";
            }

            TemaForo tema = new TemaForo();
            tema.setTitulo(titulo);
            tema.setContenido(contenido);
            tema.setCategoria(categoriaOpt.get());
            tema.setAutor(usuario);

            TemaForo temaCreado = foroServicio.crearTema(tema);

            // Guardar archivos adjuntos si los hay
            if (archivos != null && archivos.length > 0) {
                for (MultipartFile archivo : archivos) {
                    if (!archivo.isEmpty()) {
                        try {
                            archivoServicio.guardarArchivo(archivo, usuario, temaCreado, null);
                            logger.info("Archivo guardado: {}", archivo.getOriginalFilename());
                        } catch (Exception e) {
                            logger.error("Error al guardar archivo: {}", e.getMessage());
                            // Continuar con los demás archivos
                        }
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Tema creado exitosamente");
            return "redirect:/foro/tema/" + temaCreado.getId();

        } catch (Exception e) {
            logger.error("Error al crear tema: ", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear el tema: " + e.getMessage());
            return "redirect:/foro/nuevo-tema?categoriaId=" + categoriaId;
        }
    }

    // Crear respuesta
    @PostMapping("/tema/{temaId}/responder")
    public String crearRespuesta(
            @PathVariable Long temaId,
            @RequestParam String contenido,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = getUsuarioAutenticado();
            Optional<TemaForo> temaOpt = foroServicio.obtenerTemaPorId(temaId);

            if (temaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tema no encontrado");
                return "redirect:/foro";
            }

            TemaForo tema = temaOpt.get();

            if (tema.getCerrado()) {
                redirectAttributes.addFlashAttribute("error", "Este tema está cerrado y no acepta nuevas respuestas");
                return "redirect:/foro/tema/" + temaId;
            }

            RespuestaForo respuesta = new RespuestaForo();
            respuesta.setContenido(contenido);
            respuesta.setTema(tema);
            respuesta.setAutor(usuario);

            RespuestaForo respuestaCreada = foroServicio.crearRespuesta(respuesta);

            // Guardar archivos adjuntos si los hay
            if (archivos != null && archivos.length > 0) {
                for (MultipartFile archivo : archivos) {
                    if (!archivo.isEmpty()) {
                        try {
                            archivoServicio.guardarArchivo(archivo, usuario, tema, respuestaCreada);
                            logger.info("Archivo guardado en respuesta: {}", archivo.getOriginalFilename());
                        } catch (Exception e) {
                            logger.error("Error al guardar archivo en respuesta: {}", e.getMessage());
                            // Continuar con los demás archivos
                        }
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Respuesta publicada exitosamente");
            return "redirect:/foro/tema/" + temaId;

        } catch (Exception e) {
            logger.error("Error al crear respuesta: ", e);
            redirectAttributes.addFlashAttribute("error", "Error al publicar respuesta: " + e.getMessage());
            return "redirect:/foro/tema/" + temaId;
        }
    }

    // Buscar temas
    @GetMapping("/buscar")
    public String buscarTemas(@RequestParam String q, Model model) {
        List<TemaForo> resultados = foroServicio.buscarTemas(q);

        model.addAttribute("resultados", resultados);
        model.addAttribute("query", q);
        model.addAttribute("usuario", getUsuarioAutenticado());

        return "foro/busqueda";
    }

    // ===== ARCHIVOS =====

    // Obtener archivo por ID
    @GetMapping("/archivo/{id}")
    public ResponseEntity<byte[]> obtenerArchivo(@PathVariable Long id) {
        logger.info("Solicitando archivo ID: {}", id);

        try {
            Optional<ArchivoForo> archivoOpt = archivoServicio.obtenerPorId(id);

            if (archivoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ArchivoForo archivo = archivoOpt.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(archivo.getMimeType()));
            headers.setContentLength(archivo.getContenido().length);

            // Para PDFs y otros archivos, sugerir descarga
            if (archivo.esPDF()) {
                headers.setContentDispositionFormData("attachment", archivo.getNombreOriginal());
            }

            // Para imágenes, mostrar inline
            if (archivo.esImagen()) {
                headers.setCacheControl("max-age=3600"); // Cache por 1 hora
            }

            return new ResponseEntity<>(archivo.getContenido(), headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error obteniendo archivo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
