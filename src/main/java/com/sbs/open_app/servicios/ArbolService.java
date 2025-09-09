/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.ArbolDTO;
import com.sbs.open_app.entidades.Arbol;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import com.sbs.open_app.repositorios.ArbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArbolService {
    
    private final ArbolRepository arbolRepository;
    private final UsuarioRepositorio usuarioRepository;
    
    public ArbolDTO crear(ArbolDTO arbolDTO) {
        Usuario usuario = usuarioRepository.findById(arbolDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Arbol arbol = convertirDTOaEntidad(arbolDTO);
        arbol.setUsuario(usuario);
        
        Arbol arbolGuardado = arbolRepository.save(arbol);
        return convertirEntidadADTO(arbolGuardado);
    }
    
    @Transactional(readOnly = true)
    public ArbolDTO obtenerPorId(Long id) {
        Arbol arbol = arbolRepository.findByIdWithRamas(id)
            .orElseThrow(() -> new RuntimeException("Arbol no encontrado"));
        return convertirEntidadADTO(arbol);
    }
    
    @Transactional(readOnly = true)
    public List<ArbolDTO> obtenerPorUsuario(Long usuarioId) {
        return arbolRepository.findByUsuarioId(usuarioId)
            .stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }
    
    public ArbolDTO actualizar(Long id, ArbolDTO arbolDTO) {
        Arbol arbol = arbolRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Arbol no encontrado"));
        
        // Actualizar campos
        arbol.setA(arbolDTO.getA());
        arbol.setB(arbolDTO.getB());
        arbol.setC(arbolDTO.getC());
        arbol.setD(arbolDTO.getD());
        arbol.setE(arbolDTO.getE());
        arbol.setF(arbolDTO.getF());
        arbol.setAf(arbolDTO.getAf());
        arbol.setBf(arbolDTO.getBf());
        arbol.setCf(arbolDTO.getCf());
        arbol.setBa(arbolDTO.isBa());
        arbol.setBb(arbolDTO.isBb());
        arbol.setBc(arbolDTO.isBc());
        arbol.setCalendario(arbolDTO.getCalendario());
        
        Arbol arbolActualizado = arbolRepository.save(arbol);
        return convertirEntidadADTO(arbolActualizado);
    }
    
    public void eliminar(Long id) {
        if (!arbolRepository.existsById(id)) {
            throw new RuntimeException("Arbol no encontrado");
        }
        arbolRepository.deleteById(id);
    }
    
    private ArbolDTO convertirEntidadADTO(Arbol arbol) {
        ArbolDTO dto = new ArbolDTO();
        dto.setId(arbol.getId());
        dto.setA(arbol.getA());
        dto.setB(arbol.getB());
        dto.setC(arbol.getC());
        dto.setD(arbol.getD());
        dto.setE(arbol.getE());
        dto.setF(arbol.getF());
        dto.setAf(arbol.getAf());
        dto.setBf(arbol.getBf());
        dto.setCf(arbol.getCf());
        dto.setBa(arbol.isBa());
        dto.setBb(arbol.isBb());
        dto.setBc(arbol.isBc());
        dto.setCalendario(arbol.getCalendario());
        dto.setUsuarioId(arbol.getUsuario() != null ? arbol.getUsuario().getId() : null);
        // Si necesitas las ramas, las puedes mapear aquí
        return dto;
    }
    
    private Arbol convertirDTOaEntidad(ArbolDTO dto) {
        Arbol arbol = new Arbol();
        arbol.setA(dto.getA());
        arbol.setB(dto.getB());
        arbol.setC(dto.getC());
        arbol.setD(dto.getD());
        arbol.setE(dto.getE());
        arbol.setF(dto.getF());
        arbol.setAf(dto.getAf());
        arbol.setBf(dto.getBf());
        arbol.setCf(dto.getCf());
        arbol.setBa(dto.isBa());
        arbol.setBb(dto.isBb());
        arbol.setBc(dto.isBc());
        arbol.setCalendario(dto.getCalendario());
        return arbol;
    }
}
