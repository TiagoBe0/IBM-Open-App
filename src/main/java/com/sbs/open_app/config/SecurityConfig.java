package com.sbs.open_app.config;

import com.sbs.open_app.servicios.UsuarioServicio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final UsuarioServicio usuarioServicio;
    private final PasswordEncoder passwordEncoder;
    
    // Inyección por constructor para evitar circular dependencies
    public SecurityConfig(UsuarioServicio usuarioServicio, PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioServicio);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF temporalmente para pruebas
            .csrf(csrf -> csrf.disable())
            
            // Configurar el proveedor de autenticación
            .authenticationProvider(authenticationProvider())
            
            // Configurar autorización
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/",
                    "/index",
                    "/login",
                    "/registro",
                    "/registrar",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/webjars/**",
                    "/test/**"  // Endpoints de prueba
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuario/**").hasAnyRole("USUARIO", "ADMIN", "MODERADOR")
                .requestMatchers("/perfil/**").authenticated()
                .requestMatchers("/dashboard/**").authenticated()
                .anyRequest().authenticated()
            )
            
            // Configurar login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Configurar logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Configurar remember me
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
                .userDetailsService(usuarioServicio)
            )
            
            // Configurar manejo de excepciones
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/error")
            )
            
            // Configurar sesiones
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );
        
        // Permitir frames para H2 Console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
}