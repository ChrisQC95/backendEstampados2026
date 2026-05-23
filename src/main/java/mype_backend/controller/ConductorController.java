package mype_backend.controller;

import mype_backend.entity.Conductor;
import mype_backend.service.ConductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/conductores")
@CrossOrigin(origins = "*")
public class ConductorController {

    @Autowired
    private ConductorService conductorService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Conductor> listar(@PathVariable Long usuarioId) {
        return conductorService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public Conductor crear(@RequestBody Conductor conductor) {
        return conductorService.guardar(conductor);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        conductorService.eliminar(id);
    }
}