package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.RegistroUsuarioDTO;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.excepciones.UsuarioException;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class UsuarioServicio implements UserDetailsService {
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Directorio para guardar imágenes
    private static final String UPLOAD_DIR = "uploads/perfiles/";
    



    // Inyección por constructor
    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO registroDTO) throws UsuarioException {
        
        // Validaciones
        validarRegistro(registroDTO);
        
        // Verificar si el email ya existe
        if (usuarioRepositorio.existsByEmail(registroDTO.getEmail())) {
            throw new UsuarioException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroDTO.getNombre());
        nuevoUsuario.setApellido(registroDTO.getApellido());
        nuevoUsuario.setEmail(registroDTO.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol(Usuario.Rol.USUARIO);
        nuevoUsuario.setActivo(true);
        
        return usuarioRepositorio.save(nuevoUsuario);
    }
    
    /**
     * Registrar usuario con foto de perfil
     */
    @Transactional
    public Usuario registrarUsuarioConFoto(RegistroUsuarioDTO registroDTO, MultipartFile foto) throws UsuarioException, IOException {
        
        Usuario usuario = registrarUsuario(registroDTO);
        
        if (foto != null && !foto.isEmpty()) {
            String nombreArchivo = guardarFotoPerfil(foto);
            usuario.setFotoPerfil(nombreArchivo);
            usuario = usuarioRepositorio.save(usuario);
        }
        
        return usuario;
    }
    
    /**
     * Validar datos de registro
     */
    private void validarRegistro(RegistroUsuarioDTO registroDTO) throws UsuarioException {
        
        if (registroDTO.getNombre() == null || registroDTO.getNombre().trim().isEmpty()) {
            throw new UsuarioException("El nombre es obligatorio");
        }
        
        if (registroDTO.getApellido() == null || registroDTO.getApellido().trim().isEmpty()) {
            throw new UsuarioException("El apellido es obligatorio");
        }
        
        if (registroDTO.getEmail() == null || registroDTO.getEmail().trim().isEmpty()) {
            throw new UsuarioException("El email es obligatorio");
        }
        
        if (!registroDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new UsuarioException("El formato del email no es válido");
        }
        
        if (registroDTO.getPassword() == null || registroDTO.getPassword().length() < 6) {
            throw new UsuarioException("La contraseña debe tener al menos 6 caracteres");
        }
        
        if (!registroDTO.passwordsMatch()) {
            throw new UsuarioException("Las contraseñas no coinciden");
        }
    }
    
    /**
     * Guardar foto de perfil
     */
    private String guardarFotoPerfil(MultipartFile foto) throws IOException {
        
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generar nombre único para el archivo
        String nombreOriginal = foto.getOriginalFilename();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        String nombreUnico = UUID.randomUUID().toString() + extension;
        
        // Guardar archivo
        Path filePath = uploadPath.resolve(nombreUnico);
        Files.copy(foto.getInputStream(), filePath);
        
        return nombreUnico;
    }
    
    /**
     * Buscar usuario por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }
    
    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }
    
    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }
    
    /**
     * Listar usuarios activos
     */
    public List<Usuario> listarActivos() {
        return usuarioRepositorio.findByActivoTrue();
    }
    
    /**
     * Actualizar usuario
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, String nombre, String apellido) throws UsuarioException {
        
        Usuario usuario = usuarioRepositorio.findById(id)
            .orElseThrow(() -> new UsuarioException("Usuario no encontrado"));
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            usuario.setNombre(nombre);
        }
        
        if (apellido != null && !apellido.trim().isEmpty()) {
            usuario.setApellido(apellido);
        }
        
        return usuarioRepositorio.save(usuario);
    }
    
    /**
     * Cambiar contraseña
     */
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) throws UsuarioException {
        
        Usuario usuario = usuarioRepositorio.findById(id)
            .orElseThrow(() -> new UsuarioException("Usuario no encontrado"));
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new UsuarioException("La contraseña actual es incorrecta");
        }
        
        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepositorio.save(usuario);
    }
    
    /**
     * Activar/Desactivar usuario
     */
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean activo) {
        usuarioRepositorio.actualizarEstadoUsuario(id, activo);
    }
    
    /**
     * Eliminar usuario
     */
    @Transactional
    public void eliminarUsuario(Long id) throws UsuarioException {
        if (!usuarioRepositorio.existsById(id)) {
            throw new UsuarioException("Usuario no encontrado");
        }
        usuarioRepositorio.deleteById(id);
    }
    
    /**
     * Implementación CORREGIDA de UserDetailsService para Spring Security
     * Este es el método crucial para el login
     */@Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + email);
        }
        
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
    }
}
