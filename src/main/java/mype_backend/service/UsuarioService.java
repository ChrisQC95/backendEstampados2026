package mype_backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import mype_backend.dto.RolResponseDTO;
import mype_backend.dto.UsuarioCreateRequest;
import mype_backend.dto.UsuarioCreateResponse;
import mype_backend.dto.UsuarioEstadoRequest;
import mype_backend.dto.UsuarioResponseDTO;
import mype_backend.dto.UsuarioUpdateRequest;
import mype_backend.dto.UsuarioUpdateRolRequest;
import mype_backend.entity.Rol;
import mype_backend.entity.Usuario;
import mype_backend.repository.RolRepository;
import mype_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios(Jwt jwt) {
        validarAdministrador(jwt);
        return usuarioRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UsuarioCreateResponse crearUsuario(Jwt jwt, UsuarioCreateRequest request) {
        validarAdministrador(jwt);
        validarCreateRequest(request);

        String email = request.email().trim().toLowerCase();
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese correo.");
        });

        Rol rol = obtenerRolActivo(request.rolId());
        validarUnicoAdmin(null, rol, true);

        FirebaseCreateResult firebaseUser = crearUsuarioFirebase(email, request.nombre());

        Usuario usuario = Usuario.builder()
                .firebaseUid(firebaseUser.uid())
                .email(email)
                .nombre(normalizarNombre(request.nombre(), email))
                .rol(rol)
                .activo(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioCreateResponse(toResponse(guardado), firebaseUser.passwordResetLink());
    }

    @Transactional
    public UsuarioResponseDTO actualizarNombre(Jwt jwt, Long id, UsuarioUpdateRequest request) {
        validarAdministrador(jwt);
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }

        Usuario usuario = obtenerUsuario(id);
        usuario.setNombre(request.nombre().trim());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizarRol(Jwt jwt, Long id, UsuarioUpdateRolRequest request) {
        Usuario adminActual = validarAdministrador(jwt);
        if (request == null || request.rolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio.");
        }

        Usuario usuario = obtenerUsuario(id);
        Rol nuevoRol = obtenerRolActivo(request.rolId());
        validarCambioDeUnicoAdmin(adminActual, usuario, nuevoRol);
        validarUnicoAdmin(usuario.getId(), nuevoRol, Boolean.TRUE.equals(usuario.getActivo()));

        usuario.setRol(nuevoRol);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstado(Jwt jwt, Long id, UsuarioEstadoRequest request) {
        Usuario adminActual = validarAdministrador(jwt);
        if (request == null || request.activo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado activo es obligatorio.");
        }

        Usuario usuario = obtenerUsuario(id);
        if (Boolean.FALSE.equals(request.activo()) && esAdmin(usuario) && usuario.getId().equals(adminActual.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes inhabilitar tu propio usuario administrador.");
        }
        if (Boolean.FALSE.equals(request.activo()) && esAdmin(usuario) && usuarioRepository.countByRolCodigoAndActivoTrue(ROL_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe existir al menos un administrador activo.");
        }
        if (Boolean.TRUE.equals(request.activo()) && esAdmin(usuario)) {
            validarUnicoAdmin(usuario.getId(), usuario.getRol(), true);
        }

        usuario.setActivo(request.activo());
        actualizarEstadoFirebase(usuario);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Usuario validarAdministrador(Jwt jwt) {
        Usuario usuario = obtenerUsuarioAutenticado(jwt);
        if (!esAdmin(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede gestionar usuarios.");
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioAutenticado(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida.");
        }

        Usuario usuario = usuarioRepository.findByFirebaseUid(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado para este sistema."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        }
        return usuario;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        Rol rol = usuario.getRol();
        RolResponseDTO rolResponse = new RolResponseDTO(
                rol.getId(),
                rol.getCodigo(),
                rol.getNombre(),
                rol.getDescripcion(),
                rol.getActivo());

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getFirebaseUid(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getActivo(),
                rolResponse);
    }

    private void validarCreateRequest(UsuarioCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del usuario son obligatorios.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio.");
        }
        if (!EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo válido.");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es obligatorio.");
        }
        if (request.rolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio.");
        }
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    private Rol obtenerRolActivo(Integer rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado."));
        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol seleccionado no está activo.");
        }
        return rol;
    }

    private void validarCambioDeUnicoAdmin(Usuario adminActual, Usuario usuario, Rol nuevoRol) {
        boolean usuarioEraAdmin = esAdmin(usuario);
        boolean usuarioSeraAdmin = ROL_ADMIN.equals(nuevoRol.getCodigo());

        if (usuarioEraAdmin && !usuarioSeraAdmin && usuario.getId().equals(adminActual.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes quitarte el rol administrador a ti mismo.");
        }
        if (usuarioEraAdmin && !usuarioSeraAdmin && usuarioRepository.countByRolCodigoAndActivoTrue(ROL_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe existir al menos un administrador activo.");
        }
    }

    private void validarUnicoAdmin(Long usuarioIdActual, Rol rol, boolean activo) {
        if (!activo || !ROL_ADMIN.equals(rol.getCodigo())) {
            return;
        }

        long adminsActivos = usuarioRepository.countByRolCodigoAndActivoTrue(ROL_ADMIN);
        if (usuarioIdActual == null && adminsActivos > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un administrador activo.");
        }
        if (usuarioIdActual != null && adminsActivos > 0) {
            Usuario usuario = obtenerUsuario(usuarioIdActual);
            boolean yaEraAdminActivo = esAdmin(usuario) && Boolean.TRUE.equals(usuario.getActivo());
            if (!yaEraAdminActivo) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un administrador activo.");
            }
        }
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol() != null && ROL_ADMIN.equals(usuario.getRol().getCodigo());
    }

    private String normalizarNombre(String nombre, String email) {
        if (nombre != null && !nombre.isBlank()) {
            return nombre.trim();
        }
        return email;
    }

    private FirebaseCreateResult crearUsuarioFirebase(String email, String nombre) {
        try {
            String passwordTemporal = generarPasswordTemporal();
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(passwordTemporal)
                    .setDisplayName(normalizarNombre(nombre, email))
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String passwordResetLink = FirebaseAuth.getInstance().generatePasswordResetLink(email);
            return new FirebaseCreateResult(userRecord.getUid(), passwordResetLink);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Firebase Admin no está inicializado. Configura credenciales del servidor para crear usuarios.",
                    e);
        } catch (FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo crear el usuario en Firebase.", e);
        }
    }

    private void actualizarEstadoFirebase(Usuario usuario) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(usuario.getFirebaseUid())
                    .setDisabled(!Boolean.TRUE.equals(usuario.getActivo()));
            FirebaseAuth.getInstance().updateUser(request);
        } catch (IllegalStateException | FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo actualizar el estado en Firebase.", e);
        }
    }

    private String generarPasswordTemporal() {
        char[] password = new char[24];
        for (int i = 0; i < password.length; i++) {
            password[i] = PASSWORD_CHARS[SECURE_RANDOM.nextInt(PASSWORD_CHARS.length)];
        }
        return new String(password);
    }

    private record FirebaseCreateResult(String uid, String passwordResetLink) {
    }
}


