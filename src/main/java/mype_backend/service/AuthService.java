package mype_backend.service;

import mype_backend.entity.Usuario;
import mype_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario verificarOSubsanarUsuario(String uid, String email, String nombre) {
        // 1. Buscar si ya existe por su Firebase UID
        Optional<Usuario> usuarioExistente = usuarioRepository.findByFirebaseUid(uid);

        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }

        // 2. Si no existe, es un usuario nuevo. Lo creamos.
        Usuario nuevoUsuario = Usuario.builder()
                .firebaseUid(uid)
                .email(email)
                .nombre(nombre)
                .build();

        return usuarioRepository.save(nuevoUsuario);
    }
}