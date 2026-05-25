package mype_backend.controller;

import mype_backend.dto.VentaRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
// @CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // Endpoint principal para registrar una venta desde el formulario del frontend
    @PostMapping
    public Venta crearVenta(@RequestBody VentaRequestDTO ventaRequestDTO) {
        return ventaService.registrarVenta(ventaRequestDTO);
    }

    // Endpoint para cargar el historial de ventas del usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Venta> listarPorUsuario(@PathVariable Long usuarioId) {
        return ventaService.listarHistorial(usuarioId);
    }
}