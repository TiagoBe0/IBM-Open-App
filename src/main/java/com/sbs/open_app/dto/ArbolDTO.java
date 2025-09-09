
package com.sbs.open_app.dto;


import lombok.Data;
import java.util.Calendar;
import java.util.List;

@Data
public class ArbolDTO {
    private Long id;
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private float af;
    private float bf;
    private float cf;
    private boolean ba;
    private boolean bb;
    private boolean bc;
    private Calendar calendario;
    private Long usuarioId;
    private List<RamaDTO> ramas;
}
