
package com.sbs.open_app.excepciones;


public class UsuarioException extends Exception {
    
    public UsuarioException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}