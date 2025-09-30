package com.sbs.open_app.controllers;


import com.sbs.open_app.dto.HojaDTO;
import com.sbs.open_app.servicios.HojaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;

import com.sbs.open_app.servicios.DocumentoService;
import com.sbs.open_app.entidades.Documento;
import com.sbs.open_app.entidades.Hoja;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.hibernate.internal.CoreLogging.logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;




@RestController
@RequestMapping("/api/hoja")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HojaController {
    
    private final HojaService hojaService;
    
    private final DocumentoService documentoService;
    
    
    private static final Logger logger = LoggerFactory.getLogger(RamaController.class);    
    
        
    @PostMapping("/registrar")
    public ResponseEntity<HojaDTO> crear(@RequestBody HojaDTO hojaDTO) {
        HojaDTO nuevaHoja = hojaService.crear(hojaDTO);
        return new ResponseEntity<>(nuevaHoja, HttpStatus.CREATED);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<HojaDTO>> crearMultiples(@RequestBody List<HojaDTO> hojasDTO) {
        List<HojaDTO> nuevasHojas = hojaService.crearMultiples(hojasDTO);
        return new ResponseEntity<>(nuevasHojas, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HojaDTO> obtenerPorId(@PathVariable Long id) {
        HojaDTO hoja = hojaService.obtenerPorId(id);
        return ResponseEntity.ok(hoja);
    }
    
    @GetMapping("/rama/{ramaId}")
    public ResponseEntity<List<HojaDTO>> obtenerPorRama(@PathVariable Long ramaId) {
        List<HojaDTO> hojas = hojaService.obtenerPorRama(ramaId);
        return ResponseEntity.ok(hojas);
    }
    
    @GetMapping("/rama/{ramaId}/activas")
    public ResponseEntity<List<HojaDTO>> obtenerActivasPorRama(@PathVariable Long ramaId) {
        List<HojaDTO> hojas = hojaService.obtenerActivasPorRama(ramaId);
        return ResponseEntity.ok(hojas);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HojaDTO> actualizar(@PathVariable Long id, @RequestBody HojaDTO hojaDTO) {
        HojaDTO hojaActualizada = hojaService.actualizar(id, hojaDTO);
        return ResponseEntity.ok(hojaActualizada);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        hojaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
@PostMapping(value = "/registrar-con-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> crearConDocumento(
        @RequestParam("hoja") String hojaJson,
        @RequestParam(value = "documento", required = false) MultipartFile documento) {
    
    logger.info("=== PETICIÓN POST HOJA CON DOCUMENTO ===");
    
    try {
        ObjectMapper mapper = new ObjectMapper();
        HojaDTO hojaDTO = mapper.readValue(hojaJson, HojaDTO.class);
        
        if (hojaDTO.getRamaId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ramaId es obligatorio"));
        }
        
        HojaDTO nuevaHoja = hojaService.crear(hojaDTO);
        
        if (documento != null && !documento.isEmpty()) {
            Documento doc = documentoService.guardarDocumento(documento);
            
            Hoja hoja = hojaService.obtenerEntidadPorId(nuevaHoja.getId());
            hoja.setDocumento(doc);
            hojaService.guardarEntidad(hoja);
            
            nuevaHoja.setDocumentoId(doc.getId());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaHoja);
        
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