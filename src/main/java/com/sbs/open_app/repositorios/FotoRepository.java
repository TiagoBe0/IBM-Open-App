
package com.sbs.open_app.repositorios;

import com.sbs.open_app.entidades.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
    
    // Buscar fotos por tipo MIME
    List<Foto> findByMimeContainingIgnoreCase(String mime);
    
    // Buscar fotos por nombre
    List<Foto> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar solo fotos de imagen
    @Query("SELECT f FROM Foto f WHERE f.mime LIKE 'image/%'")
    List<Foto> findImagenesOnly();
    
    // Verificar si existe una foto con un nombre específico
    boolean existsByNombre(String nombre);
}