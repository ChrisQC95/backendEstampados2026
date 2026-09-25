package mype_backend.controller;

import mype_backend.dto.RolResponseDTO;
import mype_backend.service.RolService;
import mype_backend.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;
    private final UsuarioService usuarioService;

    public RolController(RolService rolService, UsuarioService usuarioService) {
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<RolResponseDTO> listarActivos(@AuthenticationPrincipal Jwt jwt) {
        usuarioService.validarAdministrador(jwt);
        return rolService.listarActivos();
    }
}
