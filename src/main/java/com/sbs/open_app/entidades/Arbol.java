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
@Table(name = "arboles")
@Data
@EqualsAndHashCode(callSuper = true)
public class Arbol extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "foto_id")
    private Foto foto;

    @OneToMany(mappedBy = "arbol", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Rama> ramas = new ArrayList<>();
}