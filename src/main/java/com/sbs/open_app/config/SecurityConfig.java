package com.sbs.open_app.config;

import com.sbs.open_app.servicios.CustomOAuth2UserService;
import com.sbs.open_app.servicios.UsuarioServicio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioServicio usuarioServicio;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(UsuarioServicio usuarioServicio,
                          PasswordEncoder passwordEncoder,
                          CustomOAuth2UserService oAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioServicio);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/", "/turnos", "/turnos/**",
                    "/api/turnos/**",
                    "/css/**", "/js/**", "/img/**", "/webjars/**",
                    "/error", "/login"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Login clásico (médico con email/contraseña)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/panel", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            // Login con Google (pacientes)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/error"));

        return http.build();
    }
}
