package com.sbs.open_app.entidades.base;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Calendar;

@MappedSuperclass
@Data
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campo_a")
    private String a;
    
    @Column(name = "campo_b")
    private String b;
    
    @Column(name = "campo_c")
    private String c;
    
    @Column(name = "campo_d")
    private String d;
    
    @Column(name = "campo_e")
    private String e;
    
    @Column(name = "campo_f")
    private String f;
    
    @Column(name = "valor_af")
    private float af;
    
    @Column(name = "valor_bf")
    private float bf;
    
    @Column(name = "valor_cf")
    private float cf;
    
    @Column(name = "flag_ba")
    private boolean ba;
    
    @Column(name = "flag_bb")
    private boolean bb;
    
    @Column(name = "flag_bc")
    private boolean bc;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "calendario")
    private Calendar calendario;
}