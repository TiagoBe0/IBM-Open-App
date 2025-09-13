package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.FotoDTO;
import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.repositorios.FotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FotoService {
    
    private final FotoRepository fotoRepository;
    
    public FotoDTO guardarFoto(MultipartFile archivo) throws IOException {
        // Validaciones
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        if (archivo.getSize() > 5 * 1024 * 1024) { // 5MB máximo
            throw new IllegalArgumentException("El archivo es demasiado grande (máximo 5MB)");
        }
        
        String mimeType = archivo.getContentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }
        
        // Crear entidad
        Foto foto = new Foto();
        foto.setNombre(archivo.getOriginalFilename());
        foto.setMime(mimeType);
        foto.setContenido(archivo.getBytes());
        
        // Guardar
        Foto fotoGuardada = fotoRepository.save(foto);
        
        return convertirADTO(fotoGuardada);
    }
    
    public Optional<FotoDTO> obtenerPorId(Long id) {
        return fotoRepository.findById(id)
                .map(this::convertirADTO);
    }
    
    public List<FotoDTO> listarMetadatos() {
        return fotoRepository.findAllMetadataOnly()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    public void eliminar(Long id) {
        if (!fotoRepository.existsById(id)) {
            throw new IllegalArgumentException("Foto no encontrada con ID: " + id);
        }
        fotoRepository.deleteById(id);
    }
    
    public List<FotoDTO> buscarPorNombre(String nombre) {
        return fotoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    private FotoDTO convertirADTO(Foto foto) {
        return new FotoDTO(
            foto.getId(),
            foto.getMime(),
            foto.getNombre(),
            foto.getContenido()
        );
    }
}