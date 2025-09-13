package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.repositorios.FotoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FotoService {

    private static final Logger logger = LoggerFactory.getLogger(FotoService.class);
    private final FotoRepository fotoRepository;

    /**
     * Guardar una foto desde un MultipartFile
     */
    public Foto guardarFoto(MultipartFile file) {
        logger.info("Guardando foto: {}", file.getOriginalFilename());

        try {
            // Validaciones
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 5MB.");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Solo se permiten archivos de imagen");
            }

            // Crear entidad Foto
            Foto foto = new Foto();
            foto.setNombre(file.getOriginalFilename());
            foto.setMime(contentType);
            foto.setContenido(file.getBytes());

            // Guardar en base de datos
            Foto fotoGuardada = fotoRepository.save(foto);
            
            logger.info("Foto guardada exitosamente con ID: {}", fotoGuardada.getId());
            return fotoGuardada;

        } catch (IOException e) {
            logger.error("Error leyendo el archivo: ", e);
            throw new RuntimeException("Error procesando el archivo: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error guardando foto: ", e);
            throw new RuntimeException("Error guardando la foto: " + e.getMessage());
        }
    }

    /**
     * Obtener foto por ID
     */
    @Transactional(readOnly = true)
    public Foto obtenerPorId(Long id) {
        logger.info("Obteniendo foto con ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }

        Optional<Foto> fotoOpt = fotoRepository.findById(id);
        if (fotoOpt.isEmpty()) {
            throw new RuntimeException("Foto no encontrada con ID: " + id);
        }

        return fotoOpt.get();
    }

    /**
     * Verificar si una foto existe
     */
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        if (id == null) return false;
        return fotoRepository.existsById(id);
    }

    /**
     * Eliminar foto por ID
     */
    public void eliminar(Long id) {
        logger.info("Eliminando foto con ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }

        if (!fotoRepository.existsById(id)) {
            throw new RuntimeException("Foto no encontrada con ID: " + id);
        }

        try {
            fotoRepository.deleteById(id);
            logger.info("Foto eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error eliminando foto: ", e);
            throw new RuntimeException("Error eliminando la foto: " + e.getMessage());
        }
    }

    /**
     * Actualizar foto existente
     */
    public Foto actualizarFoto(Long id, MultipartFile file) {
        logger.info("Actualizando foto con ID: {}", id);

        // Verificar que la foto existe
        Foto fotoExistente = obtenerPorId(id);

        try {
            // Validaciones del nuevo archivo
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 5MB.");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Solo se permiten archivos de imagen");
            }

            // Actualizar datos
            fotoExistente.setNombre(file.getOriginalFilename());
            fotoExistente.setMime(contentType);
            fotoExistente.setContenido(file.getBytes());

            // Guardar cambios
            Foto fotoActualizada = fotoRepository.save(fotoExistente);
            
            logger.info("Foto actualizada exitosamente");
            return fotoActualizada;

        } catch (IOException e) {
            logger.error("Error leyendo el archivo: ", e);
            throw new RuntimeException("Error procesando el archivo: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error actualizando foto: ", e);
            throw new RuntimeException("Error actualizando la foto: " + e.getMessage());
        }
    }

    /**
     * Obtener información básica de la foto (sin contenido binario)
     */
    @Transactional(readOnly = true)
    public FotoInfo obtenerInfo(Long id) {
        Foto foto = obtenerPorId(id);
        
        return new FotoInfo(
            foto.getId(),
            foto.getNombre(),
            foto.getMime(),
            foto.getContenido().length
        );
    }

    /**
     * DTO para información básica de foto
     */
    public static class FotoInfo {
        private final Long id;
        private final String nombre;
        private final String mime;
        private final int tamaño;

        public FotoInfo(Long id, String nombre, String mime, int tamaño) {
            this.id = id;
            this.nombre = nombre;
            this.mime = mime;
            this.tamaño = tamaño;
        }

        // Getters
        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getMime() { return mime; }
        public int getTamaño() { return tamaño; }
    }
}