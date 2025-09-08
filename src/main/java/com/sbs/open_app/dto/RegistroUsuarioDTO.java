package com.sbs.open_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroUsuarioDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;
    
    // Constructor vacío
    public RegistroUsuarioDTO() {
        System.out.println("📦 DTO creado");
    }
    
    // Getters y Setters con logs
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        System.out.println("📝 DTO - Seteando nombre: " + nombre);
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        System.out.println("📝 DTO - Seteando apellido: " + apellido);
        this.apellido = apellido;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        System.out.println("📝 DTO - Seteando email: " + email);
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        System.out.println("📝 DTO - Seteando password (length): " + (password != null ? password.length() : 0));
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        System.out.println("📝 DTO - Seteando confirmPassword (length): " + (confirmPassword != null ? confirmPassword.length() : 0));
        this.confirmPassword = confirmPassword;
    }
    
    // Validación personalizada
    public boolean passwordsMatch() {
        boolean match = password != null && password.equals(confirmPassword);
        System.out.println("🔐 DTO - Passwords match: " + match);
        return match;
    }
    
    @Override
    public String toString() {
        return "RegistroUsuarioDTO{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", passwordLength=" + (password != null ? password.length() : 0) +
                ", confirmPasswordLength=" + (confirmPassword != null ? confirmPassword.length() : 0) +
                '}';
    }
}