package com.sbs.open_app.controllers;

import com.sbs.open_app.dto.FotoDTO;
import com.sbs.open_app.servicios.FotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/foto")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FotoController {
    
    private final FotoService fotoService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> subirFoto(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Subiendo foto: {}", file.getOriginalFilename());
            FotoDTO fotoGuardada = fotoService.guardarFoto(file);
            return ResponseEntity.ok(fotoGuardada);
        } catch (Exception e) {
            log.error("Error al subir foto: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        try {
            Optional<FotoDTO> fotoOpt = fotoService.obtenerPorId(id);
            
            if (fotoOpt.isPresent()) {
                FotoDTO foto = fotoOpt.get();
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(foto.getMime()));
                headers.setContentLength(foto.getContenido().length);
                
                return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al obtener foto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/metadata")
    public ResponseEntity<List<FotoDTO>> listarMetadatos() {
        try {
            List<FotoDTO> metadatos = fotoService.listarMetadatos();
            return ResponseEntity.ok(metadatos);
        } catch (Exception e) {
            log.error("Error al listar metadatos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFoto(@PathVariable Long id) {
        try {
            fotoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar foto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}