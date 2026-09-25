package mype_backend.controller;

import mype_backend.entity.CuentaBancaria;
import mype_backend.service.CuentaBancariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas-bancarias")
public class CuentaBancariaController {

    @Autowired
    private CuentaBancariaService service;

    // Obtener las cuentas registradas por el usuario actual
    @GetMapping("/usuario/{usuarioId}")
    public List<CuentaBancaria> listarTodas(@PathVariable Long usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    // Crear o actualizar datos bancarios
    @PostMapping
    public CuentaBancaria guardar(@RequestBody CuentaBancaria cuenta) {
        return service.guardar(cuenta);
    }

    // Eliminar registro bancario
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
