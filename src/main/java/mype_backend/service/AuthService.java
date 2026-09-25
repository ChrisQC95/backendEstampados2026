package mype_backend.service;

import mype_backend.entity.Usuario;
import mype_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario sincronizarUsuarioAutenticado(Jwt jwt, String nombreFallback) {
        String uid = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String nombre = resolverNombre(jwt, nombreFallback, email);

        if (uid == null || uid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token Firebase no contiene un UID válido.");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token Firebase no contiene un email válido.");
        }

        Usuario usuario = usuarioRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para este sistema."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        }

        usuario.setEmail(email);
        usuario.setNombre(nombre);
        return usuarioRepository.save(usuario);
    }

    private String resolverNombre(Jwt jwt, String nombreFallback, String email) {
        String nombreToken = jwt.getClaimAsString("name");
        if (nombreToken != null && !nombreToken.isBlank()) {
            return nombreToken;
        }
        if (nombreFallback != null && !nombreFallback.isBlank()) {
            return nombreFallback;
        }
        return email;
    }
}
