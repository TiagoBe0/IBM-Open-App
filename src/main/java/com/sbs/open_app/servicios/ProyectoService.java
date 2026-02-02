package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.ProyectoDTO;
import com.sbs.open_app.entidades.Proyecto;
import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import com.sbs.open_app.repositorios.ProyectoRepository;
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
public class ProyectoService {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoService.class);

    @Autowired
    private final ProyectoRepository proyectoRepository;

    @Autowired
    private final UsuarioRepositorio usuarioRepository;

    @Autowired
    private final FotoRepository fotoRepository;

    @Transactional
    public ProyectoDTO crear(ProyectoDTO proyectoDTO) {
        logger.info("Creando proyecto: {}", proyectoDTO.getNombre());

        if (proyectoDTO.getUsuarioId() == null) {
            throw new IllegalArgumentException("El campo usuarioId es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(proyectoDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + proyectoDTO.getUsuarioId()));

        Proyecto proyecto = convertirDTOaEntidad(proyectoDTO);
        proyecto.setUsuario(usuario);

        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);
        logger.info("Proyecto guardado exitosamente con ID: {}", proyectoGuardado.getId());

        return convertirEntidadADTO(proyectoGuardado);
    }

    @Transactional(readOnly = true)
    public ProyectoDTO obtenerPorId(Long id) {
        logger.info("Buscando proyecto con ID: {}", id);

        Proyecto proyecto = proyectoRepository.findByIdWithTareas(id)
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));

        return convertirEntidadADTO(proyecto);
    }

    @Transactional(readOnly = true)
    public List<ProyectoDTO> obtenerPorUsuario(Long usuarioId) {
        logger.info("Buscando proyectos del usuario ID: {}", usuarioId);

        List<Proyecto> proyectos = proyectoRepository.findByUsuarioId(usuarioId);
        logger.info("Encontrados {} proyectos para el usuario {}", proyectos.size(), usuarioId);

        return proyectos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }

    public Proyecto obtenerEntidadPorId(Long id) {
        return proyectoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    public void guardarEntidad(Proyecto proyecto) {
        proyectoRepository.save(proyecto);
    }

    public void eliminar(Long id) {
        logger.info("Eliminando proyecto ID: {}", id);

        if (!proyectoRepository.existsById(id)) {
            throw new RuntimeException("Proyecto no encontrado con ID: " + id);
        }

        proyectoRepository.deleteById(id);
        logger.info("Proyecto eliminado exitosamente");
    }

    private ProyectoDTO convertirEntidadADTO(Proyecto proyecto) {
        ProyectoDTO dto = new ProyectoDTO();
        dto.setId(proyecto.getId());
        dto.setNombre(proyecto.getNombre());
        dto.setDescripcion(proyecto.getDescripcion());
        dto.setFechaInicio(proyecto.getFechaInicio());
        dto.setFechaFin(proyecto.getFechaFin());
        dto.setEstado(proyecto.getEstado());
        dto.setPrioridad(proyecto.getPrioridad());
        dto.setColor(proyecto.getColor());
        dto.setUsuarioId(proyecto.getUsuario() != null ? proyecto.getUsuario().getId() : null);
        dto.setFotoId(proyecto.getFoto() != null ? proyecto.getFoto().getId() : null);
        dto.setDocumentoId(proyecto.getDocumento() != null ? proyecto.getDocumento().getId() : null);

        return dto;
    }

    private Proyecto convertirDTOaEntidad(ProyectoDTO dto) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(dto.getNombre());
        proyecto.setDescripcion(dto.getDescripcion());
        proyecto.setFechaInicio(dto.getFechaInicio());
        proyecto.setFechaFin(dto.getFechaFin());
        proyecto.setEstado(dto.getEstado());
        proyecto.setPrioridad(dto.getPrioridad());
        proyecto.setColor(dto.getColor());

        if (dto.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(dto.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + dto.getFotoId()));
                proyecto.setFoto(foto);
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", dto.getFotoId(), e.getMessage());
            }
        }

        return proyecto;
    }

    public ProyectoDTO actualizar(Long id, ProyectoDTO proyectoDTO) {
        logger.info("Actualizando proyecto ID: {}", id);

        Proyecto proyecto = proyectoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));

        proyecto.setNombre(proyectoDTO.getNombre());
        proyecto.setDescripcion(proyectoDTO.getDescripcion());
        proyecto.setFechaInicio(proyectoDTO.getFechaInicio());
        proyecto.setFechaFin(proyectoDTO.getFechaFin());
        proyecto.setEstado(proyectoDTO.getEstado());
        proyecto.setPrioridad(proyectoDTO.getPrioridad());
        proyecto.setColor(proyectoDTO.getColor());

        if (proyectoDTO.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(proyectoDTO.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + proyectoDTO.getFotoId()));
                proyecto.setFoto(foto);
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", proyectoDTO.getFotoId(), e.getMessage());
            }
        } else {
            proyecto.setFoto(null);
        }

        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);
        logger.info("Proyecto actualizado exitosamente");

        return convertirEntidadADTO(proyectoActualizado);
    }
}
