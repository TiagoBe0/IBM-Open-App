package com.sbs.open_app.controladores;


import com.sbs.open_app.dto.ArbolDTO;
import com.sbs.open_app.servicios.ArbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/arboles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArbolController {
    
    private final ArbolService arbolService;
    
    @PostMapping
    public ResponseEntity<ArbolDTO> crear(@RequestBody ArbolDTO arbolDTO) {
        ArbolDTO nuevoArbol = arbolService.crear(arbolDTO);
        return new ResponseEntity<>(nuevoArbol, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArbolDTO> obtenerPorId(@PathVariable Long id) {
        ArbolDTO arbol = arbolService.obtenerPorId(id);
        return ResponseEntity.ok(arbol);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ArbolDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<ArbolDTO> arboles = arbolService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(arboles);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ArbolDTO> actualizar(@PathVariable Long id, @RequestBody ArbolDTO arbolDTO) {
        ArbolDTO arbolActualizado = arbolService.actualizar(id, arbolDTO);
        return ResponseEntity.ok(arbolActualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        arbolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}