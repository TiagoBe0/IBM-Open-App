package com.sbs.open_app.controladores;

import com.sbs.open_app.dto.RegistroUsuarioDTO;
import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.excepciones.UsuarioException;
import com.sbs.open_app.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class AuthController {
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    /**
     * Mostrar página de inicio
     */
    @GetMapping("/")
    public String home(Model model) {
        System.out.println("📍 Accediendo a página de inicio");
        return "index";
    }
    
    /**
     * Mostrar formulario de login
     */
    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        
        System.out.println("📍 Mostrando página de login");
        
        if (error != null) {
            System.out.println("❌ Error de login detectado");
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
        
        if (logout != null) {
            System.out.println("✅ Usuario cerró sesión");
            model.addAttribute("mensaje", "Has cerrado sesión exitosamente");
        }
        
        if (expired != null) {
            System.out.println("⏰ Sesión expirada");
            model.addAttribute("error", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente");
        }
        
        return "login";
    }
    
    /**
     * Mostrar formulario de registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        System.out.println("📍 Mostrando página de registro");
        RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
        model.addAttribute("usuario", dto);
        return "registro";
    }
    
    /**
     * Procesar registro de usuario
     */
    @PostMapping("/registrar")
    public String registrarUsuario(
            @Valid @ModelAttribute("usuario") RegistroUsuarioDTO registroDTO,
            BindingResult result,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        System.out.println("🔍 INICIANDO PROCESO DE REGISTRO");
        System.out.println("📧 Email: " + registroDTO.getEmail());
        System.out.println("👤 Nombre: " + registroDTO.getNombre());
        System.out.println("👤 Apellido: " + registroDTO.getApellido());
        System.out.println("🔑 Password length: " + (registroDTO.getPassword() != null ? registroDTO.getPassword().length() : 0));
        System.out.println("🔑 Confirm Password length: " + (registroDTO.getConfirmPassword() != null ? registroDTO.getConfirmPassword().length() : 0));
        
        // Verificar errores de validación
        if (result.hasErrors()) {
            System.out.println("❌ Errores de validación encontrados:");
            result.getAllErrors().forEach(error -> {
                System.out.println("   - " + error.getDefaultMessage());
            });
            return "registro";
        }
        
        // Verificar que las contraseñas coincidan
        if (!registroDTO.passwordsMatch()) {
            System.out.println("❌ Las contraseñas no coinciden");
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return "registro";
        }
        
        try {
            Usuario nuevoUsuario;
            
            // Registrar con o sin foto
            if (foto != null && !foto.isEmpty()) {
                System.out.println("📷 Registrando con foto");
                nuevoUsuario = usuarioServicio.registrarUsuarioConFoto(registroDTO, foto);
            } else {
                System.out.println("📝 Registrando sin foto");
                nuevoUsuario = usuarioServicio.registrarUsuario(registroDTO);
            }
            
            System.out.println("✅ USUARIO REGISTRADO EXITOSAMENTE:");
            System.out.println("   - ID: " + nuevoUsuario.getId());
            System.out.println("   - Email: " + nuevoUsuario.getEmail());
            System.out.println("   - Rol: " + nuevoUsuario.getRol());
            System.out.println("   - Activo: " + nuevoUsuario.getActivo());
            
            redirectAttributes.addFlashAttribute("exito", 
                "¡Registro exitoso! Por favor, inicia sesión con tus credenciales.");
            
            return "redirect:/login";
            
        } catch (UsuarioException e) {
            System.err.println("❌ Error de usuario: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "registro";
        } catch (IOException e) {
            System.err.println("❌ Error de IO: " + e.getMessage());
            model.addAttribute("error", "Error al guardar la foto de perfil");
            return "registro";
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error inesperado: " + e.getMessage());
            return "registro";
        }
    }
    
    /**
     * Dashboard después del login
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        System.out.println("📍 Accediendo al dashboard");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) auth.getPrincipal();
                System.out.println("✅ Usuario autenticado: " + usuario.getEmail());
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            } else {
                System.err.println("❌ No se pudo obtener el usuario autenticado");
                return "redirect:/login";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
        
        return "dashboard";
    }
        @GetMapping("/registro-arbol")
    public String registroArbol(Model model) {
        System.out.println("📍 Accediendo al dashboard");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) auth.getPrincipal();
                System.out.println("✅ Usuario autenticado: " + usuario.getEmail());
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            } else {
                System.err.println("❌ No se pudo obtener el usuario autenticado");
                return "redirect:/dashboard";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard";
        }
        
        return "registroArbol.html";
    }
    /**
     * Perfil del usuario
     */
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        System.out.println("📍 Accediendo al perfil");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) auth.getPrincipal();
                model.addAttribute("usuario", usuario);
            } else {
                return "redirect:/login";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar perfil: " + e.getMessage());
            return "redirect:/login";
        }
        
        return "perfil";
    }
}