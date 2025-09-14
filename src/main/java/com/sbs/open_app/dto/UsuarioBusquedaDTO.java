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
    private String estadoAmistad; // null, PENDIENTE, ACEPTADA, RECHAZADA, BLOQUEADA
    private Boolean solicitudEnviada; // true si el usuario actual envió solicitud
    private Boolean solicitudRecibida; // true si el usuario actual recibió solicitud


}