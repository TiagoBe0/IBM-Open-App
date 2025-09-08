package com.sbs.open_app;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@SpringBootApplication
public class OpenAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenAppApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("✅ Aplicación iniciada correctamente!");
        System.out.println("🌐 Accede a: http://localhost:8080");
        System.out.println("🗄️  Base de datos: PostgreSQL - openappv1");
        System.out.println("===========================================\n");
    }
    
    @Bean
    CommandLineRunner init(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // Verificar si ya existen usuarios
                if (usuarioRepositorio.count() == 0) {
                    System.out.println("📝 Creando usuarios de prueba...");
                    
                    // Crear usuario administrador
                    Usuario admin = new Usuario();
                    admin.setNombre("Admin");
                    admin.setApellido("Sistema");
                    admin.setEmail("admin@demo.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRol(Usuario.Rol.ADMIN);
                    admin.setActivo(true);
                    admin.setFechaRegistro(LocalDateTime.now());
                    usuarioRepositorio.save(admin);
                    
                    // Crear usuario normal
                    Usuario usuario = new Usuario();
                    usuario.setNombre("Juan");
                    usuario.setApellido("Pérez");
                    usuario.setEmail("usuario@demo.com");
                    usuario.setPassword(passwordEncoder.encode("usuario123"));
                    usuario.setRol(Usuario.Rol.USUARIO);
                    usuario.setActivo(true);
                    usuario.setFechaRegistro(LocalDateTime.now());
                    usuarioRepositorio.save(usuario);
                    
                    // Crear usuario moderador
                    Usuario moderador = new Usuario();
                    moderador.setNombre("María");
                    moderador.setApellido("González");
                    moderador.setEmail("moderador@demo.com");
                    moderador.setPassword(passwordEncoder.encode("mod123"));
                    moderador.setRol(Usuario.Rol.MODERADOR);
                    moderador.setActivo(true);
                    moderador.setFechaRegistro(LocalDateTime.now());
                    usuarioRepositorio.save(moderador);
                    
                    System.out.println("\n===========================================");
                    System.out.println("✅ USUARIOS DE PRUEBA CREADOS:");
                    System.out.println("-------------------------------------------");
                    System.out.println("👤 Admin: admin@demo.com / admin123");
                    System.out.println("👤 Usuario: usuario@demo.com / usuario123");
                    System.out.println("👤 Moderador: moderador@demo.com / mod123");
                    System.out.println("===========================================\n");
                } else {
                    System.out.println("\n📋 Usuarios existentes en la base de datos:");
                    System.out.println("-------------------------------------------");
                    usuarioRepositorio.findAll().forEach(u -> {
                        System.out.println("👤 " + u.getEmail() + " - Rol: " + u.getRol() + " - Activo: " + u.getActivo());
                    });
                    System.out.println("-------------------------------------------\n");
                }
            } catch (Exception e) {
                System.err.println("❌ Error al crear usuarios de prueba: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}