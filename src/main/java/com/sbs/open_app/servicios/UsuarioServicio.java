package com.sbs.open_app.servicios;

import com.sbs.open_app.dto.RegistroUsuarioDTO;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.excepciones.UsuarioException;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioServicio implements UserDetailsService {
    
    private UsuarioRepositorio usuarioRepositorio;
    
    private PasswordEncoder passwordEncoder;
    
    // Directorio para guardar imágenes
    private static final String UPLOAD_DIR = "uploads/perfiles/";
    
    // Constructor para inyección
    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Verificar usuarios al iniciar
     */
    
    
    
    
    // Autenticar usuario
    public Usuario autenticar(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Si las contraseñas están hasheadas
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return usuario;
            }
            
            // Si las contraseñas están en texto plano (solo para desarrollo)
            // NOTA: En producción siempre usar hashing
            if (password.equals(usuario.getPassword())) {
                return usuario;
            }
        }
        
        return null;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    @PostConstruct
    public void verificarUsuarios() {
        System.out.println("\n=== VERIFICACIÓN DE USUARIOS EN BASE DE DATOS ===");
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        System.out.println("Total de usuarios: " + usuarios.size());
        
        for (Usuario u : usuarios) {
            System.out.println("---");
            System.out.println("ID: " + u.getId());
            System.out.println("Email: " + u.getEmail());
            System.out.println("Nombre: " + u.getNombre() + " " + u.getApellido());
            System.out.println("Activo: " + u.getActivo());
            System.out.println("Rol: " + u.getRol());
            System.out.println("Fecha Registro: " + u.getFechaRegistro());
        }
        System.out.println("===========================================\n");
    }
    
    /**
     * Registrar nuevo usuario
     */
    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO registroDTO) throws UsuarioException {
        System.out.println("🔍 SERVICIO: Iniciando registro de usuario");
        System.out.println("   Email: " + registroDTO.getEmail());
        
        // Validaciones
        validarRegistro(registroDTO);
        
        // Verificar si el email ya existe
        if (usuarioRepositorio.existsByEmail(registroDTO.getEmail())) {
            System.err.println("❌ Email ya registrado: " + registroDTO.getEmail());
            throw new UsuarioException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroDTO.getNombre());
        nuevoUsuario.setApellido(registroDTO.getApellido());
        nuevoUsuario.setEmail(registroDTO.getEmail());
        
        // Encriptar contraseña
        String passwordEncriptada = passwordEncoder.encode(registroDTO.getPassword());
        System.out.println("🔐 Password encriptada (primeros 10 chars): " + passwordEncriptada.substring(0, 10) + "...");
        nuevoUsuario.setPassword(passwordEncriptada);
        
        nuevoUsuario.setRol(Usuario.Rol.USUARIO);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        
        System.out.println("💾 Guardando usuario en base de datos...");
        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);
        
        System.out.println("✅ Usuario guardado con ID: " + usuarioGuardado.getId());
        
        // Verificar que se guardó correctamente
        Optional<Usuario> verificacion = usuarioRepositorio.findById(usuarioGuardado.getId());
        if (verificacion.isPresent()) {
            System.out.println("✅ Verificación exitosa - Usuario existe en BD");
        } else {
            System.err.println("❌ ERROR: Usuario no se encuentra en BD después de guardar");
        }
        
        return usuarioGuardado;
    }
    
    /**
     * Registrar usuario con foto de perfil
     */
    @Transactional
    public Usuario registrarUsuarioConFoto(RegistroUsuarioDTO registroDTO, MultipartFile foto) throws UsuarioException, IOException {
        System.out.println("📷 Registrando usuario con foto");
        
        Usuario usuario = registrarUsuario(registroDTO);
        
        if (foto != null && !foto.isEmpty()) {
            String nombreArchivo = guardarFotoPerfil(foto);
            usuario.setFotoPerfil(nombreArchivo);
            usuario = usuarioRepositorio.save(usuario);
            System.out.println("✅ Foto guardada: " + nombreArchivo);
        }
        
        return usuario;
    }
    
    /**
     * Validar datos de registro
     */
    private void validarRegistro(RegistroUsuarioDTO registroDTO) throws UsuarioException {
        System.out.println("🔍 Validando datos de registro...");
        
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
        
        System.out.println("✅ Validación exitosa");
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
     * Implementación de UserDetailsService para Spring Security
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 LOGIN: Buscando usuario con email: " + email);
        
        Usuario usuario = usuarioRepositorio.findByEmail(email)
            .orElseThrow(() -> {
                System.err.println("❌ LOGIN: Usuario no encontrado: " + email);
                return new UsernameNotFoundException("Usuario no encontrado: " + email);
            });

        System.out.println("✅ LOGIN: Usuario encontrado:");
        System.out.println("   - ID: " + usuario.getId());
        System.out.println("   - Email: " + usuario.getEmail());
        System.out.println("   - Activo: " + usuario.isActivo());
        System.out.println("   - Rol: " + usuario.getRol());
        System.out.println("   - Password hash (primeros 10): " + usuario.getPassword().substring(0, 10) + "...");
        
        if (!usuario.isActivo()) {
            System.err.println("❌ LOGIN: Usuario desactivado: " + email);
            throw new UsernameNotFoundException("Usuario desactivado: " + email);
        }

        System.out.println("✅ LOGIN: Usuario autorizado para login");
        return usuario;
    }
    
    // Otros métodos...
    
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }
    
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }
    
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }
    
    public List<Usuario> listarActivos() {
        return usuarioRepositorio.findByActivoTrue();
    }
    
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
    
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) throws UsuarioException {
        Usuario usuario = usuarioRepositorio.findById(id)
            .orElseThrow(() -> new UsuarioException("Usuario no encontrado"));
        
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new UsuarioException("La contraseña actual es incorrecta");
        }
        
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepositorio.save(usuario);
    }
    
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean activo) {
        usuarioRepositorio.actualizarEstadoUsuario(id, activo);
    }
    
    @Transactional
    public void eliminarUsuario(Long id) throws UsuarioException {
        if (!usuarioRepositorio.existsById(id)) {
            throw new UsuarioException("Usuario no encontrado");
        }
        usuarioRepositorio.deleteById(id);
    }
}