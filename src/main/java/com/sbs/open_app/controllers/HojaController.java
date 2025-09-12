package com.sbs.open_app.controllers;


import com.sbs.open_app.dto.HojaDTO;
import com.sbs.open_app.servicios.HojaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hoja")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HojaController {
    
    private final HojaService hojaService;
    
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
}