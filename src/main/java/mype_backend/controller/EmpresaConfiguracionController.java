package mype_backend.controller;

import mype_backend.dto.EmpresaPerfilDTO;
import mype_backend.service.EmpresaConfiguracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa-configuracion")
// @CrossOrigin(origins = "*")
public class EmpresaConfiguracionController {

    @Autowired
    private EmpresaConfiguracionService service;

    // Obtener todo el perfil de la empresa unificado
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<EmpresaPerfilDTO> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPerfil(usuarioId));
    }

    // Guardar los datos dividiéndolos en sus tablas respectivas
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody EmpresaPerfilDTO dto) {
        try {
            return ResponseEntity.ok(service.guardarOActualizar(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}