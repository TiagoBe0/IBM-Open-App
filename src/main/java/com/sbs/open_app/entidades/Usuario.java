package com.sbs.open_app.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@EqualsAndHashCode(callSuper = false)
public class Usuario implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(
        name = "usuario_seq",
        sequenceName = "usuario_sequence",
        initialValue = 100,
        allocationSize = 1
    )
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.USUARIO;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now(); // Valor por defecto aquí
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "foto_perfil")
    private String fotoPerfil;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Arbol> arboles = new ArrayList<>();
    
    // Enum para roles
    public enum Rol {
        USUARIO, ADMIN, MODERADOR
    }
    
    // Constructor vacío personalizado (reemplaza @NoArgsConstructor)
    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.rol = Rol.USUARIO;
        this.arboles = new ArrayList<>();
    }
    
    // Constructor completo personalizado (reemplaza @AllArgsConstructor)
    public Usuario(String nombre, String apellido, String email, String password, Rol rol) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.rol = rol != null ? rol : Rol.USUARIO;
    }
    
    // Métodos de utilidad
    public void addArbol(Arbol arbol) {
        if (arboles == null) {
            arboles = new ArrayList<>();
        }
        arboles.add(arbol);
        arbol.setUsuario(this);
    }
    
    public void removeArbol(Arbol arbol) {
        if (arboles != null) {
            arboles.remove(arbol);
            arbol.setUsuario(null);
        }
    }
    
    // Implementación de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return activo != null ? activo : false;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return activo != null ? activo : false;
    }
    
    // Métodos adicionales
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
    
    public Boolean isActivo() {
        return activo != null ? activo : false;
    }
}