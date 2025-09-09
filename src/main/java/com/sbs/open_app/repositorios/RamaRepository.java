package com.sbs.open_app.repositorios;


import com.sbs.open_app.entidades.Rama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RamaRepository extends JpaRepository<Rama, Long> {
    
    List<Rama> findByArbolId(Long arbolId);
    
    @Query("SELECT r FROM Rama r LEFT JOIN FETCH r.hojas WHERE r.id = :id")
    Optional<Rama> findByIdWithHojas(@Param("id") Long id);
    
    @Query("SELECT r FROM Rama r WHERE r.arbol.id = :arbolId AND r.a = :valorA")
    List<Rama> findByArbolIdAndA(@Param("arbolId") Long arbolId, @Param("valorA") String valorA);
    
    void deleteByIdAndArbolId(Long id, Long arbolId);
}