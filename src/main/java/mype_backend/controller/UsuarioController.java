package mype_backend.controller;

import mype_backend.dto.UsuarioCreateRequest;
import mype_backend.dto.UsuarioCreateResponse;
import mype_backend.dto.UsuarioEstadoRequest;
import mype_backend.dto.UsuarioResponseDTO;
import mype_backend.dto.UsuarioUpdateRequest;
import mype_backend.dto.UsuarioUpdateRolRequest;
import mype_backend.repository.UsuarioRepository;
import mype_backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/firebase/{uid}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorFirebaseUid(
            @PathVariable String uid,
            @AuthenticationPrincipal Jwt jwt) {
        if (!jwt.getSubject().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar otro usuario.");
        }

        usuarioService.obtenerUsuarioAutenticado(jwt);
        return usuarioRepository.findByFirebaseUid(uid)
                .map(usuarioService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin")
    public List<UsuarioResponseDTO> listarUsuarios(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.listarUsuarios(jwt);
    }

    @PostMapping("/admin")
    public ResponseEntity<UsuarioCreateResponse> crearUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(jwt, request));
    }

    @PutMapping("/admin/{id}")
    public UsuarioResponseDTO actualizarNombre(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizarNombre(jwt, id, request);
    }

    @PutMapping("/admin/{id}/rol")
    public UsuarioResponseDTO actualizarRol(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRolRequest request) {
        return usuarioService.actualizarRol(jwt, id, request);
    }

    @PutMapping("/admin/{id}/estado")
    public UsuarioResponseDTO cambiarEstado(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody UsuarioEstadoRequest request) {
        return usuarioService.cambiarEstado(jwt, id, request);
    }
}
