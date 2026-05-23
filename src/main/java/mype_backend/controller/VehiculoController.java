package mype_backend.controller;

import mype_backend.entity.Vehiculo;
import mype_backend.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Vehiculo> listar(@PathVariable Long usuarioId) {
        return vehiculoService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public Vehiculo crear(@RequestBody Vehiculo vehiculo) {
        return vehiculoService.guardar(vehiculo);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
    }
}