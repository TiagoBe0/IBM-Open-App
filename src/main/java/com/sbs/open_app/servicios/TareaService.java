package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.TareaDTO;
import com.sbs.open_app.entidades.Tarea;
import com.sbs.open_app.entidades.Proyecto;
import com.sbs.open_app.entidades.Foto;
import com.sbs.open_app.repositorios.TareaRepository;
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
public class TareaService {

    private static final Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private final TareaRepository tareaRepository;

    @Autowired
    private final ProyectoRepository proyectoRepository;

    @Autowired
    private final FotoRepository fotoRepository;

    @Transactional
    public TareaDTO crear(TareaDTO tareaDTO) {
        logger.info("Creando tarea: {}", tareaDTO.getTitulo());

        if (tareaDTO.getProyectoId() == null) {
            throw new IllegalArgumentException("El campo proyectoId es obligatorio");
        }

        Proyecto proyecto = proyectoRepository.findById(tareaDTO.getProyectoId())
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + tareaDTO.getProyectoId()));

        Tarea tarea = convertirDTOaEntidad(tareaDTO);
        tarea.setProyecto(proyecto);

        Tarea tareaGuardada = tareaRepository.save(tarea);
        logger.info("Tarea guardada exitosamente con ID: {}", tareaGuardada.getId());

        return convertirEntidadADTO(tareaGuardada);
    }

    @Transactional(readOnly = true)
    public TareaDTO obtenerPorId(Long id) {
        logger.info("Buscando tarea con ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

        return convertirEntidadADTO(tarea);
    }

    @Transactional(readOnly = true)
    public List<TareaDTO> obtenerPorProyecto(Long proyectoId) {
        logger.info("Buscando tareas del proyecto ID: {}", proyectoId);

        List<Tarea> tareas = tareaRepository.findByProyectoId(proyectoId);
        logger.info("Encontradas {} tareas para el proyecto {}", tareas.size(), proyectoId);

        return tareas.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }

    public Tarea obtenerEntidadPorId(Long id) {
        return tareaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public void guardarEntidad(Tarea tarea) {
        tareaRepository.save(tarea);
    }

    public void eliminar(Long id) {
        logger.info("Eliminando tarea ID: {}", id);

        if (!tareaRepository.existsById(id)) {
            throw new RuntimeException("Tarea no encontrada con ID: " + id);
        }

        tareaRepository.deleteById(id);
        logger.info("Tarea eliminada exitosamente");
    }

    private TareaDTO convertirEntidadADTO(Tarea tarea) {
        TareaDTO dto = new TareaDTO();
        dto.setId(tarea.getId());
        dto.setTitulo(tarea.getTitulo());
        dto.setDescripcion(tarea.getDescripcion());
        dto.setFechaVencimiento(tarea.getFechaVencimiento());
        dto.setFechaCreacion(tarea.getFechaCreacion());
        dto.setEstado(tarea.getEstado());
        dto.setPrioridad(tarea.getPrioridad());
        dto.setCompletada(tarea.isCompletada());
        dto.setAsignadoA(tarea.getAsignadoA());
        dto.setEtiquetas(tarea.getEtiquetas());
        dto.setProyectoId(tarea.getProyecto() != null ? tarea.getProyecto().getId() : null);
        dto.setFotoId(tarea.getFoto() != null ? tarea.getFoto().getId() : null);
        dto.setDocumentoId(tarea.getDocumento() != null ? tarea.getDocumento().getId() : null);

        return dto;
    }

    private Tarea convertirDTOaEntidad(TareaDTO dto) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(dto.getTitulo());
        tarea.setDescripcion(dto.getDescripcion());
        tarea.setFechaVencimiento(dto.getFechaVencimiento());
        tarea.setEstado(dto.getEstado());
        tarea.setPrioridad(dto.getPrioridad());
        tarea.setCompletada(dto.isCompletada());
        tarea.setAsignadoA(dto.getAsignadoA());
        tarea.setEtiquetas(dto.getEtiquetas());

        if (dto.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(dto.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + dto.getFotoId()));
                tarea.setFoto(foto);
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", dto.getFotoId(), e.getMessage());
            }
        }

        return tarea;
    }

    public TareaDTO actualizar(Long id, TareaDTO tareaDTO) {
        logger.info("Actualizando tarea ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

        tarea.setTitulo(tareaDTO.getTitulo());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setFechaVencimiento(tareaDTO.getFechaVencimiento());
        tarea.setEstado(tareaDTO.getEstado());
        tarea.setPrioridad(tareaDTO.getPrioridad());
        tarea.setCompletada(tareaDTO.isCompletada());
        tarea.setAsignadoA(tareaDTO.getAsignadoA());
        tarea.setEtiquetas(tareaDTO.getEtiquetas());

        if (tareaDTO.getFotoId() != null) {
            try {
                Foto foto = fotoRepository.findById(tareaDTO.getFotoId())
                    .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + tareaDTO.getFotoId()));
                tarea.setFoto(foto);
            } catch (Exception e) {
                logger.warn("No se pudo cargar la foto con ID {}: {}", tareaDTO.getFotoId(), e.getMessage());
            }
        } else {
            tarea.setFoto(null);
        }

        Tarea tareaActualizada = tareaRepository.save(tarea);
        logger.info("Tarea actualizada exitosamente");

        return convertirEntidadADTO(tareaActualizada);
    }
}
