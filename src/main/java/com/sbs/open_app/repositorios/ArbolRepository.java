package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Arbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArbolRepository extends JpaRepository<Arbol, Long> {
    
    List<Arbol> findByUsuarioId(Long usuarioId);
    
    @Query("SELECT a FROM Arbol a LEFT JOIN FETCH a.ramas WHERE a.id = :id")
    Optional<Arbol> findByIdWithRamas(@Param("id") Long id);
    
    @Query("SELECT a FROM Arbol a WHERE a.usuario.id = :usuarioId AND a.a = :valorA")
    List<Arbol> findByUsuarioIdAndA(@Param("usuarioId") Long usuarioId, @Param("valorA") String valorA);
    
    void deleteByIdAndUsuarioId(Long id, Long usuarioId);
}

