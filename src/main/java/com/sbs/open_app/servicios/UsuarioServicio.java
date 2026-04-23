package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea el usuario administrador al iniciar si no existe.
     * Cambiá el email y la contraseña aquí antes de poner en producción.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void crearAdminInicial() {
        String emailAdmin = "admin@consultorio.com";
        if (!usuarioRepositorio.existsByEmail(emailAdmin)) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("Médico");
            admin.setEmail(emailAdmin);
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepositorio.save(admin);
            System.out.println("[TurnosMedicos] Admin creado: " + emailAdmin + " / admin1234");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepositorio.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }
}
