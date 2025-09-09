
package com.sbs.open_app.entidades;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sbs.open_app.entidades.base.BaseEntity;

@Entity
@Table(name = "hojas")
@Data
@EqualsAndHashCode(callSuper = true)
public class Hoja extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rama_id")
    @JsonBackReference
    private Rama rama;
}