package mype_backend.controller;

import mype_backend.dto.RolResponseDTO;
import mype_backend.service.RolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/roles")
public class PublicRolController {

    private final RolService rolService;

    public PublicRolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping("/sistema")
    public List<RolResponseDTO> listarRolesSistema() {
        return rolService.listarRolesSistemaPublicos();
    }

    @GetMapping("/tienda")
    public List<RolResponseDTO> listarRolesTienda() {
        return rolService.listarRolesTiendaPublicos();
    }
}
