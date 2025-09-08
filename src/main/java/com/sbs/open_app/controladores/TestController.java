package com.sbs.open_app.controladores;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class TestController {
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Endpoint de prueba para verificar usuarios
     * Accede a: http://localhost:8080/test/usuarios
     */
    @GetMapping("/test/usuarios")
    public Map<String, Object> testUsuarios() {
        Map<String, Object> response = new HashMap<>();
        
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        response.put("total_usuarios", usuarios.size());
        response.put("usuarios", usuarios.stream().map(u -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("email", u.getEmail());
            userMap.put("nombre", u.getNombre() + " " + u.getApellido());
            userMap.put("rol", u.getRol());
            userMap.put("activo", u.getActivo());
            userMap.put("password_hash", u.getPassword().substring(0, 20) + "...");
            return userMap;
        }).toList());
        
        return response;
    }
    
    /**
     * Endpoint para verificar contraseña
     * Accede a: http://localhost:8080/test/verificar-password?email=admin@demo.com&password=admin123
     */
    @GetMapping("/test/verificar-password")
    public Map<String, Object> verificarPassword(
            @RequestParam String email,
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean passwordCorrecta = passwordEncoder.matches(password, usuario.getPassword());
            
            response.put("usuario_encontrado", true);
            response.put("email", usuario.getEmail());
            response.put("activo", usuario.getActivo());
            response.put("password_correcta", passwordCorrecta);
            response.put("rol", usuario.getRol());
            
            if (!passwordCorrecta) {
                response.put("nota", "La contraseña no coincide. Verifica que estés usando la contraseña correcta.");
            }
        } else {
            response.put("usuario_encontrado", false);
            response.put("mensaje", "No se encontró usuario con email: " + email);
        }
        
        return response;
    }
    
    /**
     * Endpoint para crear un usuario de prueba manualmente
     * Accede a: http://localhost:8080/test/crear-usuario?email=test@demo.com&password=test123
     */
    @GetMapping("/test/crear-usuario")
    public Map<String, Object> crearUsuarioPrueba(
            @RequestParam String email,
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (usuarioRepositorio.existsByEmail(email)) {
            response.put("error", "Ya existe un usuario con ese email");
            return response;
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre("Test");
        usuario.setApellido("Usuario");
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(Usuario.Rol.USUARIO);
        usuario.setActivo(true);
        
        Usuario saved = usuarioRepositorio.save(usuario);
        
        response.put("mensaje", "Usuario creado exitosamente");
        response.put("id", saved.getId());
        response.put("email", saved.getEmail());
        response.put("password_plain", password);
        response.put("nota", "Ahora puedes hacer login con estas credenciales");
        
        return response;
    }
}