package com.sbs.open_app.servicios;


import com.sbs.open_app.dto.HojaDTO;
import com.sbs.open_app.entidades.Hoja;
import com.sbs.open_app.entidades.Rama;
import com.sbs.open_app.repositorios.HojaRepository;
import com.sbs.open_app.repositorios.RamaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HojaService {
    
    private final HojaRepository hojaRepository;
    private final RamaRepository ramaRepository;
    
    public HojaDTO crear(HojaDTO hojaDTO) {
        Rama rama = ramaRepository.findById(hojaDTO.getRamaId())
            .orElseThrow(() -> new RuntimeException("Rama no encontrada"));
        
        Hoja hoja = convertirDTOaEntidad(hojaDTO);
        hoja.setRama(rama);
        
        Hoja hojaGuardada = hojaRepository.save(hoja);
        return convertirEntidadADTO(hojaGuardada);
    }
    
    @Transactional(readOnly = true)
    public HojaDTO obtenerPorId(Long id) {
        Hoja hoja = hojaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
        return convertirEntidadADTO(hoja);
    }
    
    @Transactional(readOnly = true)
    public List<HojaDTO> obtenerPorRama(Long ramaId) {
        return hojaRepository.findByRamaId(ramaId)
            .stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<HojaDTO> obtenerActivasPorRama(Long ramaId) {
        return hojaRepository.findActiveByRamaId(ramaId)
            .stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
    }
    
    public HojaDTO actualizar(Long id, HojaDTO hojaDTO) {
        Hoja hoja = hojaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
        
        // Actualizar campos
        hoja.setA(hojaDTO.getA());
        hoja.setB(hojaDTO.getB());
        hoja.setC(hojaDTO.getC());
        hoja.setD(hojaDTO.getD());
        hoja.setE(hojaDTO.getE());
        hoja.setF(hojaDTO.getF());
        hoja.setAf(hojaDTO.getAf());
        hoja.setBf(hojaDTO.getBf());
        hoja.setCf(hojaDTO.getCf());
        hoja.setBa(hojaDTO.isBa());
        hoja.setBb(hojaDTO.isBb());
        hoja.setBc(hojaDTO.isBc());
        hoja.setCalendario(hojaDTO.getCalendario());
        
        Hoja hojaActualizada = hojaRepository.save(hoja);
        return convertirEntidadADTO(hojaActualizada);
    }
    
    public void eliminar(Long id) {
        if (!hojaRepository.existsById(id)) {
            throw new RuntimeException("Hoja no encontrada");
        }
        hojaRepository.deleteById(id);
    }
    
    public List<HojaDTO> crearMultiples(List<HojaDTO> hojasDTO) {
        return hojasDTO.stream()
            .map(this::crear)
            .collect(Collectors.toList());
    }
    
    private HojaDTO convertirEntidadADTO(Hoja hoja) {
        HojaDTO dto = new HojaDTO();
        dto.setId(hoja.getId());
        dto.setA(hoja.getA());
        dto.setB(hoja.getB());
        dto.setC(hoja.getC());
        dto.setD(hoja.getD());
        dto.setE(hoja.getE());
        dto.setF(hoja.getF());
        dto.setAf(hoja.getAf());
        dto.setBf(hoja.getBf());
        dto.setCf(hoja.getCf());
        dto.setBa(hoja.isBa());
        dto.setBb(hoja.isBb());
        dto.setBc(hoja.isBc());
        dto.setCalendario(hoja.getCalendario());
        dto.setRamaId(hoja.getRama() != null ? hoja.getRama().getId() : null);
        return dto;
    }
    
    private Hoja convertirDTOaEntidad(HojaDTO dto) {
        Hoja hoja = new Hoja();
        hoja.setA(dto.getA());
        hoja.setB(dto.getB());
        hoja.setC(dto.getC());
        hoja.setD(dto.getD());
        hoja.setE(dto.getE());
        hoja.setF(dto.getF());
        hoja.setAf(dto.getAf());
        hoja.setBf(dto.getBf());
        hoja.setCf(dto.getCf());
        hoja.setBa(dto.isBa());
        hoja.setBb(dto.isBb());
        hoja.setBc(dto.isBc());
        hoja.setCalendario(dto.getCalendario());
        return hoja;
    }
}
