package mype_backend.controller;

import mype_backend.entity.ProductoServicio;
import mype_backend.service.ProductoServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoServicioController {

    @Autowired
    private ProductoServicioService productoServicioService;

    @GetMapping("/usuario/{usuarioId}")
    public List<ProductoServicio> listar(@PathVariable Long usuarioId) {
        return productoServicioService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public ProductoServicio crear(@RequestBody ProductoServicio producto) {
        return productoServicioService.guardar(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoServicioService.eliminar(id);
    }
}
