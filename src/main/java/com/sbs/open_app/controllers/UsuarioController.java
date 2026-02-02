package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import com.sbs.open_app.servicios.FotoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones relacionadas con usuarios
 */
@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioRepositorio usuarioRepositorio;
    private final FotoService fotoService;

    /**
     * Subir foto de perfil (avatar) del usuario
     */
    @PostMapping("/{usuarioId}/avatar")
    public ResponseEntity<?> subirAvatar(
            @PathVariable Long usuarioId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        logger.info("=== SUBIENDO AVATAR PARA USUARIO {} ===", usuarioId);

        try {
            // Buscar usuario
            Usuario usuario = usuarioRepositorio.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que el usuario autenticado es el mismo
            if (authentication != null) {
                Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
                if (!usuarioAutenticado.getId().equals(usuarioId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createErrorResponse("No tiene permisos para modificar este perfil"));
                }
            }

            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El archivo está vacío"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El archivo es demasiado grande. Máximo 5MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Solo se permiten archivos de imagen"));
            }

            // Guardar la foto
            Foto fotoGuardada = fotoService.guardarFoto(file);

            // Actualizar usuario con la URL/ID de la foto
            usuario.setFotoPerfil("/api/foto/" + fotoGuardada.getId());
            usuarioRepositorio.save(usuario);

            logger.info("Avatar actualizado exitosamente para usuario {}", usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fotoUrl", usuario.getFotoPerfil());
            response.put("message", "Avatar actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error subiendo avatar: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado subiendo avatar: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor"));
        }
    }

    /**
     * Subir banner del perfil del usuario
     */
    @PostMapping("/{usuarioId}/banner")
    public ResponseEntity<?> subirBanner(
            @PathVariable Long usuarioId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        logger.info("=== SUBIENDO BANNER PARA USUARIO {} ===", usuarioId);

        try {
            // Buscar usuario
            Usuario usuario = usuarioRepositorio.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que el usuario autenticado es el mismo
            if (authentication != null) {
                Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
                if (!usuarioAutenticado.getId().equals(usuarioId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createErrorResponse("No tiene permisos para modificar este perfil"));
                }
            }

            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El archivo está vacío"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El archivo es demasiado grande. Máximo 5MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Solo se permiten archivos de imagen"));
            }

            // Guardar la foto
            Foto fotoGuardada = fotoService.guardarFoto(file);

            // Actualizar usuario con la URL/ID de la foto
            usuario.setBannerUrl("/api/foto/" + fotoGuardada.getId());
            usuarioRepositorio.save(usuario);

            logger.info("Banner actualizado exitosamente para usuario {}", usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bannerUrl", usuario.getBannerUrl());
            response.put("message", "Banner actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error subiendo banner: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado subiendo banner: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor"));
        }
    }

    /**
     * Obtener información del perfil del usuario
     */
    @GetMapping("/{usuarioId}/perfil")
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long usuarioId) {
        logger.info("=== OBTENIENDO PERFIL DE USUARIO {} ===", usuarioId);

        try {
            Usuario usuario = usuarioRepositorio.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> perfil = new HashMap<>();
            perfil.put("id", usuario.getId());
            perfil.put("nombre", usuario.getNombre());
            perfil.put("apellido", usuario.getApellido());
            perfil.put("email", usuario.getEmail());
            perfil.put("fotoPerfil", usuario.getFotoPerfil());
            perfil.put("bannerUrl", usuario.getBannerUrl());

            return ResponseEntity.ok(perfil);

        } catch (RuntimeException e) {
            logger.error("Error obteniendo perfil: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Listar todos los usuarios (básico)
     */
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        logger.info("=== LISTANDO TODOS LOS USUARIOS ===");

        try {
            var usuarios = usuarioRepositorio.findAll();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            logger.error("Error listando usuarios: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error obteniendo lista de usuarios"));
        }
    }

    /**
     * Helper para crear respuestas de error
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return error;
    }
}
