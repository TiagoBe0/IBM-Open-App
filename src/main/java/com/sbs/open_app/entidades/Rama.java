/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sbs.open_app.entidades;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sbs.open_app.entidades.base.BaseEntity;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ramas")
@Data
@EqualsAndHashCode(callSuper = true)
public class Rama extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbol_id")
    @JsonBackReference
    private Arbol arbol;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "foto_id")
    private Foto foto;

    @OneToMany(mappedBy = "rama", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Hoja> hojas = new ArrayList<>();
}

