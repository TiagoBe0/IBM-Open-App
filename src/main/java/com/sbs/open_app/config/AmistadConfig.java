package com.sbs.open_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Configuración específica para el sistema de amistades
 */
@Configuration
@ConfigurationProperties(prefix = "amistad")
@Validated
public class AmistadConfig {
    
    /**
     * Configuraciones de límites
     */
    private Limite limite = new Limite();
    
    /**
     * Configuraciones de búsqueda
     */
    private Busqueda busqueda = new Busqueda();
    
    /**
     * Configuraciones de mensajes
     */
    private Mensaje mensaje = new Mensaje();
    
    // Getters y Setters
    public Limite getLimite() { return limite; }
    public void setLimite(Limite limite) { this.limite = limite; }
    
    public Busqueda getBusqueda() { return busqueda; }
    public void setBusqueda(Busqueda busqueda) { this.busqueda = busqueda; }
    
    public Mensaje getMensaje() { return mensaje; }
    public void setMensaje(Mensaje mensaje) { this.mensaje = mensaje; }
    
    /**
     * Configuraciones de límites del sistema
     */
    public static class Limite {
        
        @Min(1)
        @Max(100)
        private int solicitudesDiarias = 10;
        
        @Min(10)
        @Max(10000)
        private int amigosMaximo = 1000;
        
        @Min(1)
        @Max(24)
        private int horasEsperaReintentoRechazo = 24;
        
        // Getters y Setters
        public int getSolicitudesDiarias() { return solicitudesDiarias; }
        public void setSolicitudesDiarias(int solicitudesDiarias) { this.solicitudesDiarias = solicitudesDiarias; }
        
        public int getAmigosMaximo() { return amigosMaximo; }
        public void setAmigosMaximo(int amigosMaximo) { this.amigosMaximo = amigosMaximo; }
        
        public int getHorasEsperaReintentoRechazo() { return horasEsperaReintentoRechazo; }
        public void setHorasEsperaReintentoRechazo(int horasEsperaReintentoRechazo) { 
            this.horasEsperaReintentoRechazo = horasEsperaReintentoRechazo; 
        }
    }
    
    /**
     * Configuraciones de búsqueda
     */
    public static class Busqueda {
        
        @Min(10)
        @Max(200)
        private int resultadosMaximo = 50;
        
        @Min(1)
        @Max(10)
        private int caracteresMinimos = 2;
        
        private boolean busquedaPorEmail = true;
        private boolean busquedaPorNombre = true;
        private boolean busquedaPorUsername = true;
        
        // Getters y Setters
        public int getResultadosMaximo() { return resultadosMaximo; }
        public void setResultadosMaximo(int resultadosMaximo) { this.resultadosMaximo = resultadosMaximo; }
        
        public int getCaracteresMinimos() { return caracteresMinimos; }
        public void setCaracteresMinimos(int caracteresMinimos) { this.caracteresMinimos = caracteresMinimos; }
        
        public boolean isBusquedaPorEmail() { return busquedaPorEmail; }
        public void setBusquedaPorEmail(boolean busquedaPorEmail) { this.busquedaPorEmail = busquedaPorEmail; }
        
        public boolean isBusquedaPorNombre() { return busquedaPorNombre; }
        public void setBusquedaPorNombre(boolean busquedaPorNombre) { this.busquedaPorNombre = busquedaPorNombre; }
        
        public boolean isBusquedaPorUsername() { return busquedaPorUsername; }
        public void setBusquedaPorUsername(boolean busquedaPorUsername) { this.busquedaPorUsername = busquedaPorUsername; }
    }
    
    /**
     * Configuraciones de mensajes
     */
    public static class Mensaje {
        
        @Min(0)
        @Max(1000)
        private int longitudMaxima = 500;
        
        @NotNull
        private String mensajePorDefecto = "¡Hola! Me gustaría agregarte como amigo.";
        
        private boolean permitirMensajesVacios = true;
        private boolean filtrarContenidoInapropiado = true;
        
        // Getters y Setters
        public int getLongitudMaxima() { return longitudMaxima; }
        public void setLongitudMaxima(int longitudMaxima) { this.longitudMaxima = longitudMaxima; }
        
        public String getMensajePorDefecto() { return mensajePorDefecto; }
        public void setMensajePorDefecto(String mensajePorDefecto) { this.mensajePorDefecto = mensajePorDefecto; }
        
        public boolean isPermitirMensajesVacios() { return permitirMensajesVacios; }
        public void setPermitirMensajesVacios(boolean permitirMensajesVacios) { 
            this.permitirMensajesVacios = permitirMensajesVacios; 
        }
        
        public boolean isFiltrarContenidoInapropiado() { return filtrarContenidoInapropiado; }
        public void setFiltrarContenidoInapropiado(boolean filtrarContenidoInapropiado) { 
            this.filtrarContenidoInapropiado = filtrarContenidoInapropiado; 
        }
    }
    
    /**
     * Método utilitario para obtener configuraciones como texto
     */
    public String getConfiguracionResumen() {
        return String.format(
            "Límites: Solicitudes diarias=%d, Amigos máximo=%d | " +
            "Búsqueda: Resultados máximo=%d, Caracteres mínimos=%d | " +
            "Mensajes: Longitud máxima=%d",
            limite.solicitudesDiarias,
            limite.amigosMaximo,
            busqueda.resultadosMaximo,
            busqueda.caracteresMinimos,
            mensaje.longitudMaxima
        );
    }
}