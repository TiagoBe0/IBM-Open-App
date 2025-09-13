
package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.ArbolDTO;
import com.sbs.open_app.entidades.Arbol;
import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import com.sbs.open_app.repositorios.ArbolRepository;
import com.sbs.open_app.repositorios.FotoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
@Transactional
public class ArbolService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArbolService.class);
    @Autowired
    private final ArbolRepository arbolRepository;
    @Autowired
    private final UsuarioRepositorio usuarioRepository;
    
  @Autowired
    private final FotoRepository fotoRepository; // AGREGAR ESTA DEPENDENCIA
    
    @Transactional
    public ArbolDTO crear(ArbolDTO arbolDTO) {
        // Log para debug
        logger.info("=== INICIANDO CREACIÓN DE ÁRBOL ===");
        logger.info("Datos recibidos: {}", arbolDTO);
        logger.info("Usuario ID recibido: {}", arbolDTO.getUsuarioId());
        
        // Validación de usuarioId
        if (arbolDTO.getUsuarioId() == null) {
            logger.error("Error: usuarioId es null");
            throw new IllegalArgumentException("El campo usuarioId es obligatorio");
        }
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(arbolDTO.getUsuarioId())
            .orElseThrow(() -> {
                logger.error("Usuario no encontrado con ID: {}", arbolDTO.getUsuarioId());
                // Listar usuarios disponibles para debug
                List<Usuario> usuarios = usuarioRepository.findAll();
                logger.info("Usuarios disponibles en la BD:");
                usuarios.forEach(u -> logger.info("- ID: {}, Username: {}", u.getId(), u.getUsername()));
                
                return new RuntimeException("Usuario no encontrado con ID: " + arbolDTO.getUsuarioId());
            });
        
        logger.info("Usuario encontrado: {} ({})", usuario.getUsername(), usuario.getId());
        
        // Crear entidad
        Arbol arbol = convertirDTOaEntidad(arbolDTO);
        arbol.setUsuario(usuario);
        
        logger.info("Árbol a guardar: Campo A={}, Usuario={}", arbol.getA(), usuario.getUsername());
        
        // Guardar
        try {
            Arbol arbolGuardado = arbolRepository.save(arbol);
            logger.info("Árbol guardado exitosamente con ID: {}", arbolGuardado.getId());
            
            ArbolDTO resultado = convertirEntidadADTO(arbolGuardado);
            logger.info("=== CREACIÓN COMPLETADA ===");
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al guardar el árbol: ", e);
            throw new RuntimeException("Error al guardar el árbol: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public ArbolDTO obtenerPorId(Long id) {
        logger.info("Buscando árbol con ID: {}", id);
        
        Arbol arbol = arbolRepository.findByIdWithRamas(id)
            .orElseThrow(() -> {
                logger.error("Árbol no encontrado con ID: {}", id);
                return new RuntimeException("Árbol no encontrado con ID: " + id);
            });
        
        return convertirEntidadADTO(arbol);
    }
    
    @Transactional(readOnly = true)
    public List<ArbolDTO> obtenerPorUsuario(Long usuarioId) {
        logger.info("Buscando árboles del usuario ID: {}", usuarioId);
        
        List<Arbol> arboles = arbolRepository.findByUsuarioId(usuarioId);
        logger.info("Encontrados {} árboles para el usuario {}", arboles.size(), usuarioId);
        
        return arboles.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }
    
    
    public void eliminar(Long id) {
        logger.info("Eliminando árbol ID: {}", id);
        
        if (!arbolRepository.existsById(id)) {
            logger.error("Intento de eliminar árbol inexistente, ID: {}", id);
            throw new RuntimeException("Árbol no encontrado con ID: " + id);
        }
        
        arbolRepository.deleteById(id);
        logger.info("Árbol eliminado exitosamente");
    }
    
   
    
    // MÉTODO convertirEntidadADTO CORREGIDO
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
        
        // AGREGAR ESTA LÍNEA PARA MAPEAR fotoId
        dto.setFotoId(arbol.getFoto() != null ? arbol.getFoto().getId() : null);
        
        return dto;
    }
    
    // MÉTODO convertirDTOaEntidad CORREGIDO
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
        
        // MANEJO CORRECTO DE LA FOTO - Cargar desde BD en lugar de crear nueva instancia
        if (dto.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(dto.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + dto.getFotoId()));
                arbol.setFoto(foto);
                logger.info("Foto asignada al árbol: ID {}", foto.getId());
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", dto.getFotoId(), e.getMessage());
                // No asignar foto si no se encuentra
            }
        }
        
        return arbol;
    }
    
    // MÉTODO actualizar CORREGIDO
    public ArbolDTO actualizar(Long id, ArbolDTO arbolDTO) {
        logger.info("Actualizando árbol ID: {}", id);
        
        Arbol arbol = arbolRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Árbol no encontrado para actualizar, ID: {}", id);
                return new RuntimeException("Árbol no encontrado con ID: " + id);
            });
        
        // Actualizar campos básicos
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
        
        // MANEJO CORRECTO DE FOTO EN ACTUALIZACIÓN
        if (arbolDTO.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(arbolDTO.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + arbolDTO.getFotoId()));
                arbol.setFoto(foto);
                logger.info("Foto actualizada en árbol: ID {}", foto.getId());
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", arbolDTO.getFotoId(), e.getMessage());
            }
        } else {
            // Si fotoId es null, remover la foto del árbol
            arbol.setFoto(null);
            logger.info("Foto removida del árbol");
        }
        
        Arbol arbolActualizado = arbolRepository.save(arbol);
        logger.info("Árbol actualizado exitosamente");
        
        return convertirEntidadADTO(arbolActualizado);
    }
}