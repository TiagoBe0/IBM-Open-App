package com.sbs.open_app.controllers;


import com.sbs.open_app.dto.RamaDTO;
import com.sbs.open_app.servicios.RamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.sbs.open_app.servicios.DocumentoService;
import com.sbs.open_app.entidades.Documento;
import com.sbs.open_app.entidades.Rama;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.hibernate.internal.CoreLogging.logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/rama")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RamaController {
    
    private final RamaService ramaService;
    private final DocumentoService documentoService;
    
    private static final Logger logger = LoggerFactory.getLogger(RamaController.class);    
    
    
    @PostMapping("/registrar")
    public ResponseEntity<RamaDTO> crear(@RequestBody RamaDTO ramaDTO) {
        RamaDTO nuevaRama = ramaService.crear(ramaDTO);
        return new ResponseEntity<>(nuevaRama, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RamaDTO> obtenerPorId(@PathVariable Long id) {
        RamaDTO rama = ramaService.obtenerPorId(id);
        return ResponseEntity.ok(rama);
    }
    
    @GetMapping("/arbol/{arbolId}")
    public ResponseEntity<List<RamaDTO>> obtenerPorArbol(@PathVariable Long arbolId) {
        List<RamaDTO> ramas = ramaService.obtenerPorArbol(arbolId);
        return ResponseEntity.ok(ramas);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RamaDTO> actualizar(@PathVariable Long id, @RequestBody RamaDTO ramaDTO) {
        RamaDTO ramaActualizada = ramaService.actualizar(id, ramaDTO);
        return ResponseEntity.ok(ramaActualizada);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ramaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
    
@PostMapping(value = "/registrar-con-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> crearConDocumento(
        @RequestParam("rama") String ramaJson,
        @RequestParam(value = "documento", required = false) MultipartFile documento) {
    
    logger.info("=== PETICIÓN POST RAMA CON DOCUMENTO ===");
    
    try {
        ObjectMapper mapper = new ObjectMapper();
        RamaDTO ramaDTO = mapper.readValue(ramaJson, RamaDTO.class);
        
        if (ramaDTO.getArbolId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "arbolId es obligatorio"));
        }
        
        RamaDTO nuevaRama = ramaService.crear(ramaDTO);
        
        if (documento != null && !documento.isEmpty()) {
            Documento doc = documentoService.guardarDocumento(documento);
            
            Rama rama = ramaService.obtenerEntidadPorId(nuevaRama.getId());
            rama.setDocumento(doc);
            ramaService.guardarEntidad(rama);
            
            nuevaRama.setDocumentoId(doc.getId());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaRama);
        
    } catch (Exception e) {
        logger.error("Error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}

@GetMapping("/documento/{id}")
public ResponseEntity<byte[]> descargarDocumento(@PathVariable Long id) {
    try {
        Documento documento = documentoService.obtenerDocumento(id);
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + documento.getNombreArchivo() + "\"")
            .body(documento.getDatos());
            
    } catch (Exception e) {
        logger.error("Error descargando documento: ", e);
        return ResponseEntity.notFound().build();
    }
}
}
