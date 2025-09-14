package com.sbs.open_app.entidades;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasMensajes {
    private long totalMensajesEnviados;
    private long totalMensajesRecibidos;
    private long mensajesNoLeidos;
    private long conversacionesActivas;
}