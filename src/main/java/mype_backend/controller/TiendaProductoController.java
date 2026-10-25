package mype_backend.controller;

import mype_backend.dto.TiendaProductoRequestDTO;
import mype_backend.dto.TiendaProductoResponseDTO;
import mype_backend.service.TiendaProductoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tienda-productos")
public class TiendaProductoController {

    private final TiendaProductoService tiendaProductoService;

    public TiendaProductoController(TiendaProductoService tiendaProductoService) {
        this.tiendaProductoService = tiendaProductoService;
    }

    @GetMapping
    public List<TiendaProductoResponseDTO> listar() {
        return tiendaProductoService.listar();
    }

    @GetMapping("/{id}")
    public TiendaProductoResponseDTO obtener(@PathVariable Long id) {
        return tiendaProductoService.obtener(id);
    }

    @PostMapping
    public TiendaProductoResponseDTO crear(@RequestBody TiendaProductoRequestDTO request) {
        return tiendaProductoService.crear(request);
    }

    @PutMapping("/{id}")
    public TiendaProductoResponseDTO actualizar(@PathVariable Long id, @RequestBody TiendaProductoRequestDTO request) {
        return tiendaProductoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        tiendaProductoService.eliminar(id);
    }
}
