
package com.sbs.open_app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoDTO {
    private Long id;
    private String mime;
    private String nombre;
    private byte[] contenido;
    
    // Constructor sin contenido para metadatos
    public FotoDTO(Long id, String mime, String nombre) {
        this.id = id;
        this.mime = mime;
        this.nombre = nombre;
        this.contenido = null;
    }
    
    // Métodos útiles
    public boolean esImagen() {
        return mime != null && mime.startsWith("image/");
    }
    
    public long getTamano() {
        return contenido != null ? contenido.length : 0;
    }
}