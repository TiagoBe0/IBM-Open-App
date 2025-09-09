
package com.sbs.open_app.repositorios;


import com.sbs.open_app.entidades.Hoja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HojaRepository extends JpaRepository<Hoja, Long> {
    
    List<Hoja> findByRamaId(Long ramaId);
    
    @Query("SELECT h FROM Hoja h WHERE h.rama.id = :ramaId AND h.a = :valorA")
    List<Hoja> findByRamaIdAndA(@Param("ramaId") Long ramaId, @Param("valorA") String valorA);
    
    @Query("SELECT h FROM Hoja h WHERE h.ba = true AND h.rama.id = :ramaId")
    List<Hoja> findActiveByRamaId(@Param("ramaId") Long ramaId);
    
    void deleteByIdAndRamaId(Long id, Long ramaId);
}
