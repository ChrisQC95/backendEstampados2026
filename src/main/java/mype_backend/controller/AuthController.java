package mype_backend.controller;

import mype_backend.dto.AuthLoginRequest;
import mype_backend.dto.UsuarioResponseDTO;
import mype_backend.entity.Usuario;
import mype_backend.service.AuthService;
import mype_backend.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public UsuarioResponseDTO login(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) AuthLoginRequest request) {
        String nombreFallback = request != null ? request.nombre() : null;
        Usuario usuario = authService.sincronizarUsuarioAutenticado(jwt, nombreFallback);
        return usuarioService.toResponse(usuario);
    }
}
