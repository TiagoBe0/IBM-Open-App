package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioBusquedaDTO {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String username;
    private String email;
    private String fotoPerfil;
    
    // Estado de la relación con el usuario que busca
    private String estadoAmistad; // NINGUNA, PENDIENTE, ACEPTADA, RECHAZADA, BLOQUEADA
    private boolean solicitudEnviada; // true si el usuario actual envió solicitud
    private boolean solicitudRecibida; // true si el usuario actual recibió solicitud
    
    // Constructor simplificado para casos básicos
    public UsuarioBusquedaDTO(Long id, String nombre, String apellido, String username, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.email = email;
        this.estadoAmistad = "NINGUNA";
        this.solicitudEnviada = false;
        this.solicitudRecibida = false;
    }
}