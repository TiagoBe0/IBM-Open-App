package com.sbs.open_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración temporal para AmistadService
 * Usar solo si no tienes el AmistadConfig original
 */
@Configuration
@ConfigurationProperties(prefix = "amistad")
public class AmistadConfig {
    
    private Limite limite = new Limite();
    private Busqueda busqueda = new Busqueda();
    private Mensaje mensaje = new Mensaje();
    
    // Getters y Setters
    public Limite getLimite() { return limite; }
    public void setLimite(Limite limite) { this.limite = limite; }
    
    public Busqueda getBusqueda() { return busqueda; }
    public void setBusqueda(Busqueda busqueda) { this.busqueda = busqueda; }
    
    public Mensaje getMensaje() { return mensaje; }
    public void setMensaje(Mensaje mensaje) { this.mensaje = mensaje; }
    
    public static class Limite {
        private int solicitudesDiarias = 10;
        private int amigosMaximo = 1000;
        private int horasEsperaReintentoRechazo = 24;
        
        public int getSolicitudesDiarias() { return solicitudesDiarias; }
        public void setSolicitudesDiarias(int solicitudesDiarias) { this.solicitudesDiarias = solicitudesDiarias; }
        
        public int getAmigosMaximo() { return amigosMaximo; }
        public void setAmigosMaximo(int amigosMaximo) { this.amigosMaximo = amigosMaximo; }
        
        public int getHorasEsperaReintentoRechazo() { return horasEsperaReintentoRechazo; }
        public void setHorasEsperaReintentoRechazo(int horasEsperaReintentoRechazo) { 
            this.horasEsperaReintentoRechazo = horasEsperaReintentoRechazo; 
        }
    }
    
    public static class Busqueda {
        private int resultadosMaximo = 50;
        private int caracteresMinimos = 2;
        private boolean busquedaPorEmail = true;
        private boolean busquedaPorNombre = true;
        private boolean busquedaPorUsername = true;
        
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
    
    public static class Mensaje {
        private int longitudMaxima = 500;
        private String mensajePorDefecto = "¡Hola! Me gustaría agregarte como amigo.";
        private boolean permitirMensajesVacios = true;
        private boolean filtrarContenidoInapropiado = true;
        
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
}