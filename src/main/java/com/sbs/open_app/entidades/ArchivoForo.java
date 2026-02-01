package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "archivos_foro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false)
    private String nombreAlmacenado;

    @Column(name = "tipo_archivo", length = 50)
    private TipoArchivo tipoArchivo;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamanio")
    private Long tamanio; // en bytes

    @Lob
    @Column(name = "contenido")
    private byte[] contenido;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    private TemaForo tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id")
    private RespuestaForo respuesta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Enum para tipos de archivo
    public enum TipoArchivo {
        IMAGEN,
        PDF,
        OTRO
    }

    // Método helper para determinar si es imagen
    public boolean esImagen() {
        return tipoArchivo == TipoArchivo.IMAGEN;
    }

    // Método helper para determinar si es PDF
    public boolean esPDF() {
        return tipoArchivo == TipoArchivo.PDF;
    }

    // Método para obtener tamaño formateado
    public String getTamanioFormateado() {
        if (tamanio == null) return "0 B";

        if (tamanio < 1024) {
            return tamanio + " B";
        } else if (tamanio < 1024 * 1024) {
            return String.format("%.2f KB", tamanio / 1024.0);
        } else {
            return String.format("%.2f MB", tamanio / (1024.0 * 1024.0));
        }
    }
}
