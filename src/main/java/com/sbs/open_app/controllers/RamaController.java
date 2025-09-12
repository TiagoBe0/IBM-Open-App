package com.sbs.open_app.controllers;


import com.sbs.open_app.dto.RamaDTO;
import com.sbs.open_app.servicios.RamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rama")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RamaController {
    
    private final RamaService ramaService;
    
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
}
