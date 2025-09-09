package com.sbs.open_app.servicios;


import com.sbs.open_app.dto.RamaDTO;
import com.sbs.open_app.entidades.Arbol;
import com.sbs.open_app.entidades.Rama;
import com.sbs.open_app.repositorios.ArbolRepository;
import com.sbs.open_app.repositorios.RamaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RamaService {
    
    private final RamaRepository ramaRepository;
    private final ArbolRepository arbolRepository;
    
    public RamaDTO crear(RamaDTO ramaDTO) {
        Arbol arbol = arbolRepository.findById(ramaDTO.getArbolId())
            .orElseThrow(() -> new RuntimeException("Arbol no encontrado"));
        
        Rama rama = convertirDTOaEntidad(ramaDTO);
        rama.setArbol(arbol);
        
        Rama ramaGuardada = ramaRepository.save(rama);
        return convertirEntidadADTO(ramaGuardada);
    }
    
    @Transactional(readOnly = true)
    public RamaDTO obtenerPorId(Long id) {
        Rama rama = ramaRepository.findByIdWithHojas(id)
            .orElseThrow(() -> new RuntimeException("Rama no encontrada"));
        return convertirEntidadADTO(rama);
    }
    
    @Transactional(readOnly = true)
    public List<RamaDTO> obtenerPorArbol(Long arbolId) {
        return ramaRepository.findByArbolId(arbolId)
            .stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }
    
    public RamaDTO actualizar(Long id, RamaDTO ramaDTO) {
        Rama rama = ramaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rama no encontrada"));
        
        // Actualizar campos
        rama.setA(ramaDTO.getA());
        rama.setB(ramaDTO.getB());
        rama.setC(ramaDTO.getC());
        rama.setD(ramaDTO.getD());
        rama.setE(ramaDTO.getE());
        rama.setF(ramaDTO.getF());
        rama.setAf(ramaDTO.getAf());
        rama.setBf(ramaDTO.getBf());
        rama.setCf(ramaDTO.getCf());
        rama.setBa(ramaDTO.isBa());
        rama.setBb(ramaDTO.isBb());
        rama.setBc(ramaDTO.isBc());
        rama.setCalendario(ramaDTO.getCalendario());
        
        Rama ramaActualizada = ramaRepository.save(rama);
        return convertirEntidadADTO(ramaActualizada);
    }
    
    public void eliminar(Long id) {
        if (!ramaRepository.existsById(id)) {
            throw new RuntimeException("Rama no encontrada");
        }
        ramaRepository.deleteById(id);
    }
    
    private RamaDTO convertirEntidadADTO(Rama rama) {
        RamaDTO dto = new RamaDTO();
        dto.setId(rama.getId());
        dto.setA(rama.getA());
        dto.setB(rama.getB());
        dto.setC(rama.getC());
        dto.setD(rama.getD());
        dto.setE(rama.getE());
        dto.setF(rama.getF());
        dto.setAf(rama.getAf());
        dto.setBf(rama.getBf());
        dto.setCf(rama.getCf());
        dto.setBa(rama.isBa());
        dto.setBb(rama.isBb());
        dto.setBc(rama.isBc());
        dto.setCalendario(rama.getCalendario());
        dto.setArbolId(rama.getArbol() != null ? rama.getArbol().getId() : null);
        // Si necesitas las hojas, las puedes mapear aquí
        return dto;
    }
    
    private Rama convertirDTOaEntidad(RamaDTO dto) {
        Rama rama = new Rama();
        rama.setA(dto.getA());
        rama.setB(dto.getB());
        rama.setC(dto.getC());
        rama.setD(dto.getD());
        rama.setE(dto.getE());
        rama.setF(dto.getF());
        rama.setAf(dto.getAf());
        rama.setBf(dto.getBf());
        rama.setCf(dto.getCf());
        rama.setBa(dto.isBa());
        rama.setBb(dto.isBb());
        rama.setBc(dto.isBc());
        rama.setCalendario(dto.getCalendario());
        return rama;
    }
}
