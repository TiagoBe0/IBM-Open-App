package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UsuarioRepositorio usuarioRepositorio;

    public CustomOAuth2UserService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(request);
        } catch (Exception e) {
            log.error("Error al obtener datos de Google: {}", e.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("google_error"), e.getMessage(), e);
        }

        try {
            procesarUsuario(oAuth2User);
        } catch (Exception e) {
            log.error("Error al registrar usuario de Google: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("registro_fallido"),
                    "No se pudo crear el usuario: " + e.getMessage(), e);
        }

        return oAuth2User;
    }

    private void procesarUsuario(OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String googleId = (String) attrs.get("sub");
        String email    = (String) attrs.get("email");
        String nombre   = strOrEmpty(attrs.get("given_name"));
        String apellido = strOrEmpty(attrs.get("family_name"));
        String foto     = (String) attrs.get("picture");

        if (googleId == null || email == null) {
            throw new IllegalStateException("Google no devolvió sub o email");
        }

        Optional<Usuario> existente = usuarioRepositorio.findByGoogleId(googleId);
        if (existente.isEmpty()) {
            existente = usuarioRepositorio.findByEmail(email);
        }

        boolean esNuevo = existente.isEmpty();
        Usuario usuario = existente.orElseGet(Usuario::new);

        usuario.setGoogleId(googleId);
        usuario.setEmail(email);
        usuario.setFotoUrl(foto);

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            usuario.setNombre(nombre.isBlank() ? "Usuario" : nombre);
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            usuario.setApellido(apellido.isBlank() ? "-" : apellido);
        }

        if (usuario.getRol() == null || !usuario.esAdmin()) {
            usuario.setRol(Usuario.Rol.PACIENTE);
        }

        usuario.setActivo(true);
        usuarioRepositorio.save(usuario);

        if (esNuevo) {
            log.info("Nuevo usuario registrado con Google: {} {} <{}>", usuario.getNombre(), usuario.getApellido(), email);
        } else {
            log.info("Login con Google de usuario existente: {}", email);
        }
    }

    private String strOrEmpty(Object val) {
        return val instanceof String s ? s.trim() : "";
    }
}
