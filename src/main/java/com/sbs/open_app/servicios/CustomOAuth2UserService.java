package com.sbs.open_app.servicios;

import com.sbs.open_app.entidades.Usuario;
import com.sbs.open_app.repositorios.UsuarioRepositorio;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepositorio usuarioRepositorio;

    public CustomOAuth2UserService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String googleId = (String) attrs.get("sub");
        String email    = (String) attrs.get("email");
        String nombre   = (String) attrs.getOrDefault("given_name", "");
        String apellido = (String) attrs.getOrDefault("family_name", "");
        String foto     = (String) attrs.get("picture");

        // Buscar por googleId primero, luego por email
        Optional<Usuario> existente = usuarioRepositorio.findByGoogleId(googleId);
        if (existente.isEmpty()) {
            existente = usuarioRepositorio.findByEmail(email);
        }

        Usuario usuario = existente.orElseGet(Usuario::new);
        usuario.setGoogleId(googleId);
        usuario.setEmail(email);
        usuario.setFotoUrl(foto);

        // Solo actualizar nombre si es nuevo o estaba vacío
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            usuario.setNombre(nombre.isBlank() ? "Paciente" : nombre);
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            usuario.setApellido(apellido);
        }

        // Los que entran por Google son PACIENTE a menos que ya sean ADMIN
        if (usuario.getRol() == null) {
            usuario.setRol(Usuario.Rol.PACIENTE);
        }
        if (!usuario.esAdmin()) {
            usuario.setRol(Usuario.Rol.PACIENTE);
        }

        usuario.setActivo(true);
        usuarioRepositorio.save(usuario);

        return oAuth2User;
    }
}
