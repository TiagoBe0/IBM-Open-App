package com.sbs.open_app.repositorios;


import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    
    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);
    
    // Verificar si existe un email
    boolean existsByEmail(String email);
    
    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();
    
    // Buscar usuarios por rol
    List<Usuario> findByRol(Usuario.Rol rol);
    
    // Buscar usuarios por nombre o apellido
    @Query("SELECT u FROM Usuario u WHERE u.nombre LIKE %:termino% OR u.apellido LIKE %:termino%")
    List<Usuario> buscarPorNombreOApellido(@Param("termino") String termino);
    
    // Activar/Desactivar usuario
    @Modifying
    @Query("UPDATE Usuario u SET u.activo = :activo WHERE u.id = :id")
    void actualizarEstadoUsuario(@Param("id") Long id, @Param("activo") Boolean activo);
    
    // Contar usuarios por rol
    Long countByRol(Usuario.Rol rol);
    
    // Buscar usuarios registrados en los últimos X días
    @Query("SELECT u FROM Usuario u WHERE u.fechaRegistro >= CURRENT_TIMESTAMP - :dias DAY")
    List<Usuario> findUsuariosRecientes(@Param("dias") int dias);
}
