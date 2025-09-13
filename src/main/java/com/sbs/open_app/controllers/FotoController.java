package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.servicios.FotoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/foto")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FotoController {

    private static final Logger logger = LoggerFactory.getLogger(FotoController.class);
    private final FotoService fotoService;

    /**
     * Subir una nueva foto
     * @param file
     */
    @PostMapping("/upload")
    public ResponseEntity<?> subirFoto(@RequestParam("file") MultipartFile file) {
        logger.info("=== SUBIENDO FOTO ===");
        logger.info("Nombre archivo: {}", file.getOriginalFilename());
        logger.info("Tamaño archivo: {} bytes", file.getSize());
        logger.info("Tipo contenido: {}", file.getContentType());

        try {
            // Validaciones básicas
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo está vacío"));
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo es demasiado grande. Máximo 5MB."));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("Solo se permiten archivos de imagen"));
            }

            // Subir foto
            Foto fotoGuardada = fotoService.guardarFoto(file);
            
            logger.info("Foto guardada exitosamente con ID: {}", fotoGuardada.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", fotoGuardada.getId());
            response.put("nombre", fotoGuardada.getNombre());
            response.put("mime", fotoGuardada.getMime());
            response.put("url", "/api/foto/" + fotoGuardada.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error subiendo foto: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * Obtener foto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        logger.info("GET request para foto ID: {}", id);

        try {
            Foto foto = fotoService.obtenerPorId(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(foto.getMime()));
            headers.setContentLength(foto.getContenido().length);
            
            // Opcional: agregar cache headers
            headers.setCacheControl("max-age=3600"); // Cache por 1 hora

            return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("Error obteniendo foto: ", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error inesperado obteniendo foto: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar foto por ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarFoto(@PathVariable Long id) {
        logger.info("DELETE request para foto ID: {}", id);

        try {
            fotoService.eliminar(id);
            logger.info("Foto eliminada exitosamente");
            
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            logger.error("Error eliminando foto: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado eliminando foto: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor"));
        }
    }

    /**
     * Obtener información de la foto (sin el contenido binario)
     */
    @GetMapping("/{id}/info")
    public ResponseEntity<?> obtenerInfoFoto(@PathVariable Long id) {
        logger.info("GET info request para foto ID: {}", id);

        try {
            Foto foto = fotoService.obtenerPorId(id);

            Map<String, Object> info = new HashMap<>();
            info.put("id", foto.getId());
            info.put("nombre", foto.getNombre());
            info.put("mime", foto.getMime());
            info.put("tamaño", foto.getContenido().length);
            info.put("url", "/api/foto/" + foto.getId());

            return ResponseEntity.ok(info);

        } catch (RuntimeException e) {
            logger.error("Error obteniendo info de foto: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para verificar que el controlador funciona
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        logger.info("Test endpoint llamado para fotos");
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "El controlador de fotos está funcionando");
        response.put("maxSize", "5MB");
        response.put("allowedTypes", "image/*");
        return ResponseEntity.ok(response);
    }

    // Método helper para crear respuestas de error consistentes
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return error;
    }
}