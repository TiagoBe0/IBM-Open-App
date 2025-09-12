
package com.sbs.open_app.controllers;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthRestController {
    
    private UsuarioServicio usuarioService;
    
    // Login via API (para aplicaciones SPA)
    @PostMapping("/loginAuth")
    public ResponseEntity<?> loginAPI(@RequestBody LoginRequest loginRequest, 
                                      HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Usuario usuario = usuarioService.autenticar(
            loginRequest.getUsername(), 
            loginRequest.getPassword()
        );
        
        if (usuario != null) {
            // Guardar en sesión
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("username", usuario.getUsername());
            
            response.put("success", true);
            response.put("usuarioId", usuario.getId());
            response.put("username", usuario.getUsername());
            response.put("email", usuario.getEmail());
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Credenciales inválidas");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    // Verificar sesión
    @GetMapping("/check")
    public ResponseEntity<?> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId != null) {
            response.put("authenticated", true);
            response.put("usuarioId", usuarioId);
            response.put("username", session.getAttribute("username"));
            return ResponseEntity.ok(response);
        } else {
            response.put("authenticated", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    // Logout via API
    @PostMapping("/logout")
    public ResponseEntity<?> logoutAPI(HttpSession session) {
        session.invalidate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sesión cerrada exitosamente");
        
        return ResponseEntity.ok(response);
    }
}
// DTO para login
class LoginRequest {
    private String username;
    private String password;
    
    // Getters y setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}