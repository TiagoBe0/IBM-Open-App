package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.*;
import com.sbs.open_app.repositorios.ArchivoForoRepositorio;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivoForoServicio {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoForoServicio.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final ArchivoForoRepositorio archivoRepositorio;

    @Transactional
    public ArchivoForo guardarArchivo(MultipartFile file, Usuario usuario, TemaForo tema, RespuestaForo respuesta) throws IOException {
        // Validaciones
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 10MB.");
        }

        String mimeType = file.getContentType();
        if (!esArchivoPermitido(mimeType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo imágenes (JPG, PNG, GIF) y PDFs.");
        }

        // Crear entidad ArchivoForo
        ArchivoForo archivo = new ArchivoForo();
        archivo.setNombreOriginal(file.getOriginalFilename());
        archivo.setNombreAlmacenado(generarNombreUnico(file.getOriginalFilename()));
        archivo.setMimeType(mimeType);
        archivo.setTamanio(file.getSize());
        archivo.setContenido(file.getBytes());
        archivo.setUsuario(usuario);
        archivo.setTema(tema);
        archivo.setRespuesta(respuesta);
        archivo.setTipoArchivo(determinarTipoArchivo(mimeType));

        logger.info("Guardando archivo: {} ({})", archivo.getNombreOriginal(), archivo.getTamanioFormateado());

        return archivoRepositorio.save(archivo);
    }

    @Transactional(readOnly = true)
    public Optional<ArchivoForo> obtenerPorId(Long id) {
        return archivoRepositorio.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ArchivoForo> obtenerArchivosPorTema(TemaForo tema) {
        return archivoRepositorio.findByTemaOrderByFechaSubidaAsc(tema);
    }

    @Transactional(readOnly = true)
    public List<ArchivoForo> obtenerArchivosPorRespuesta(RespuestaForo respuesta) {
        return archivoRepositorio.findByRespuestaOrderByFechaSubidaAsc(respuesta);
    }

    @Transactional
    public void eliminarArchivo(Long id) {
        archivoRepositorio.deleteById(id);
    }

    // Métodos auxiliares

    private boolean esArchivoPermitido(String mimeType) {
        if (mimeType == null) return false;

        return mimeType.startsWith("image/") || mimeType.equals("application/pdf");
    }

    private ArchivoForo.TipoArchivo determinarTipoArchivo(String mimeType) {
        if (mimeType == null) return ArchivoForo.TipoArchivo.OTRO;

        if (mimeType.startsWith("image/")) {
            return ArchivoForo.TipoArchivo.IMAGEN;
        } else if (mimeType.equals("application/pdf")) {
            return ArchivoForo.TipoArchivo.PDF;
        } else {
            return ArchivoForo.TipoArchivo.OTRO;
        }
    }

    private String generarNombreUnico(String nombreOriginal) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";

        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        return uuid + extension;
    }
}
