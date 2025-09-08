
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
        
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión exitosamente");
        }
        
        if (expired != null) {
            model.addAttribute("error", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente");
        }
        
        return "login";
    }
    
    /**
     * Mostrar formulario de registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDTO());
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
        
        // Verificar errores de validación
        if (result.hasErrors()) {
            return "registro";
        }
        
        // Verificar que las contraseñas coincidan
        if (!registroDTO.passwordsMatch()) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return "registro";
        }
        
        try {
            Usuario nuevoUsuario;
            
            // Registrar con o sin foto
            if (foto != null && !foto.isEmpty()) {
                nuevoUsuario = usuarioServicio.registrarUsuarioConFoto(registroDTO, foto);
            } else {
                nuevoUsuario = usuarioServicio.registrarUsuario(registroDTO);
            }
            
            redirectAttributes.addFlashAttribute("exito", 
                "Registro exitoso! Por favor, inicia sesión con tus credenciales.");
            return "redirect:/login";
            
        } catch (UsuarioException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la foto de perfil");
            return "registro";
        }
    }
    
    /**
     * Dashboard después del login
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) auth.getPrincipal();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return "dashboard";
    }
    
    /**
     * Perfil del usuario
     */
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) auth.getPrincipal();
        
        model.addAttribute("usuario", usuario);
        return "perfil";
    }
}