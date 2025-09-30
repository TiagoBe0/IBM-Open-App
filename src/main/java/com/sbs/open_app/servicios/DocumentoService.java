package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.Documento;
import com.sbs.open_app.repositorios.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    
    private final DocumentoRepository documentoRepository;
    
    public Documento guardarDocumento(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        // Validar que sea PDF
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF");
        }
        
        Documento documento = new Documento();
        documento.setNombreArchivo(file.getOriginalFilename());
        documento.setTipoContenido(file.getContentType());
        documento.setTamano(file.getSize());
        documento.setDatos(file.getBytes());
        
        return documentoRepository.save(documento);
    }
    
    public Documento obtenerDocumento(Long id) {
        return documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }
    
    public void eliminarDocumento(Long id) {
        documentoRepository.deleteById(id);
    }
}