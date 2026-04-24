package com.sbs.open_app.config;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepositorio usuarioRepositorio;

    public OAuth2SuccessHandler(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        String destino = usuarioRepositorio.findByEmail(email)
            .filter(Usuario::esAdmin)
            .map(u -> "/admin/panel")
            .orElse("/turnos");

        response.sendRedirect(destino);
    }
}
