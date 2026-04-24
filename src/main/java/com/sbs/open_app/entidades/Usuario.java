package com.sbs.open_app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@EqualsAndHashCode(callSuper = false)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_sequence", initialValue = 1000000, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String email;

    // Nullable: pacientes con Google login no tienen password local
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.PACIENTE;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "activo")
    private Boolean activo = true;

    // Identificador único de Google (sub)
    @Column(name = "google_id", unique = true)
    private String googleId;

    // Foto de perfil de Google
    @Column(name = "foto_url")
    private String fotoUrl;

    public enum Rol {
        ADMIN, PACIENTE
    }

    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.rol = Rol.PACIENTE;
    }

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
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return activo != null && activo; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return activo != null && activo; }

    public String getNombreCompleto() {
        String n = nombre != null ? nombre.trim() : "";
        String a = apellido != null ? apellido.trim() : "";
        return (n + " " + a).trim();
    }

    public boolean esAdmin() {
        return Rol.ADMIN.equals(rol);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (activo == null) activo = true;
        if (rol == null) rol = Rol.PACIENTE;
    }
}
