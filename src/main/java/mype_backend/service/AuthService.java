package mype_backend.service;

import mype_backend.entity.Rol;
import mype_backend.entity.Usuario;
import mype_backend.repository.RolRepository;
import mype_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String ROL_CLIENTE_TIENDA = "CLIENTE_TIENDA";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public Usuario sincronizarUsuarioAutenticado(Jwt jwt, String nombreFallback) {
        DatosToken datosToken = resolverDatosToken(jwt, nombreFallback);

        Usuario usuario = usuarioRepository.findByFirebaseUid(datosToken.uid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para este sistema."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        }

        usuario.setEmail(datosToken.email());
        usuario.setNombre(datosToken.nombre());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario sincronizarClienteTiendaAutenticado(Jwt jwt, String nombreFallback) {
        DatosToken datosToken = resolverDatosToken(jwt, nombreFallback);
        Rol rolClienteTienda = rolRepository.findByCodigo(ROL_CLIENTE_TIENDA)
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El rol de cliente tienda no está configurado."));

        return usuarioRepository.findByFirebaseUid(datosToken.uid())
                .map(usuario -> validarClienteTienda(usuario, datosToken))
                .orElseGet(() -> crearClienteTienda(datosToken, rolClienteTienda));
    }

    private Usuario validarClienteTienda(Usuario usuario, DatosToken datosToken) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        }
        if (usuario.getRol() == null || !ROL_CLIENTE_TIENDA.equals(usuario.getRol().getCodigo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta cuenta no pertenece al acceso de tienda.");
        }

        usuario.setEmail(datosToken.email());
        usuario.setNombre(datosToken.nombre());
        return usuarioRepository.save(usuario);
    }

    private Usuario crearClienteTienda(DatosToken datosToken, Rol rolClienteTienda) {
        usuarioRepository.findByEmailIgnoreCase(datosToken.email())
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Este correo ya está vinculado a otra cuenta.");
                });

        Usuario usuario = Usuario.builder()
                .firebaseUid(datosToken.uid())
                .email(datosToken.email())
                .nombre(datosToken.nombre())
                .activo(true)
                .rol(rolClienteTienda)
                .build();

        return usuarioRepository.save(usuario);
    }

    private DatosToken resolverDatosToken(Jwt jwt, String nombreFallback) {
        String uid = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String nombre = resolverNombre(jwt, nombreFallback, email);

        if (uid == null || uid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token Firebase no contiene un UID válido.");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token Firebase no contiene un email válido.");
        }

        return new DatosToken(uid, email, nombre);
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

    private record DatosToken(String uid, String email, String nombre) {
    }
}
