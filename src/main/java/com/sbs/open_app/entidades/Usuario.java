package com.sbs.open_app.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
        initialValue = 1000000,
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
    private LocalDateTime fechaRegistro;
    
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
    
    // Constructor vacío personalizado
    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.rol = Rol.USUARIO;
        this.arboles = new ArrayList<>();
    }
    
    // Constructor completo personalizado
    public Usuario(String nombre, String apellido, String email, String password, Rol rol) {
        this(); // Llama al constructor vacío para inicializar valores por defecto
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.rol = rol != null ? rol : Rol.USUARIO;
    }
    
    // Métodos de utilidad para manejar la relación con Arboles
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
    
    // Implementación completa de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
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
    
    // Métodos de utilidad adicionales
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null && !nombre.trim().isEmpty()) {
            nombreCompleto.append(nombre.trim());
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellido.trim());
        }
        return nombreCompleto.toString();
    }
    
    public Boolean isActivo() {
        return activo != null ? activo : false;
    }
    
    // Método para verificar si es administrador
    public boolean isAdmin() {
        return Rol.ADMIN.equals(this.rol);
    }
    
    // Método para verificar si es moderador
    public boolean isModerador() {
        return Rol.MODERADOR.equals(this.rol);
    }
    
    // Inicialización antes de persistir
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (rol == null) {
            rol = Rol.USUARIO;
        }
        if (arboles == null) {
            arboles = new ArrayList<>();
        }
    }
    
    // Método toString personalizado (override de Lombok si es necesario)
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}