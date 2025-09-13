package com.sbs.open_app.dto;


public class ChatStats {
    private Long totalMensajes;
    private Long usuariosActivos;
    
    // Constructor vacío
    public ChatStats() {}
    
    // Constructor con parámetros
    public ChatStats(Long totalMensajes, Long usuariosActivos) {
        this.totalMensajes = totalMensajes;
        this.usuariosActivos = usuariosActivos;
    }
    
    // Getters y Setters
    public Long getTotalMensajes() {
        return totalMensajes;
    }
    
    public void setTotalMensajes(Long totalMensajes) {
        this.totalMensajes = totalMensajes;
    }
    
    public Long getUsuariosActivos() {
        return usuariosActivos;
    }
    
    public void setUsuariosActivos(Long usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }
    
    @Override
    public String toString() {
        return "ChatStats{" +
                "totalMensajes=" + totalMensajes +
                ", usuariosActivos=" + usuariosActivos +
                '}';
    }
}