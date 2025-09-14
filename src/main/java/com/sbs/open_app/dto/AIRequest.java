package com.sbs.open_app.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * DTO para solicitudes al servicio de IA local
 */
public class AIRequest {
    
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;
    
    private String usuarioNombre;
    private String tipo = "general";
    private Map<String, Object> parametros;
    
    // Constructores
    public AIRequest() {}
    
    public AIRequest(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public AIRequest(String mensaje, String tipo) {
        this.mensaje = mensaje;
        this.tipo = tipo;
    }
    
    // Getters y Setters
    public String getMensaje() { 
        return mensaje; 
    }
    
    public void setMensaje(String mensaje) { 
        this.mensaje = mensaje; 
    }
    
    public String getUsuarioNombre() { 
        return usuarioNombre; 
    }
    
    public void setUsuarioNombre(String usuarioNombre) { 
        this.usuarioNombre = usuarioNombre; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
    
    public Map<String, Object> getParametros() { 
        return parametros; 
    }
    
    public void setParametros(Map<String, Object> parametros) { 
        this.parametros = parametros; 
    }
    
    @Override
    public String toString() {
        return "AIRequest{" +
                "mensaje='" + mensaje + '\'' +
                ", usuarioNombre='" + usuarioNombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", parametros=" + parametros +
                '}';
    }
}