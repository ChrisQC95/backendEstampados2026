package mype_backend.controller;

import mype_backend.entity.Serie;
import mype_backend.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/series")
// @CrossOrigin(origins = "*")
public class SerieController {

    @Autowired
    private SerieService serieService;

    // Obtener todas las series del usuario (Para la pantalla de configuración de
    // series)
    @GetMapping("/usuario/{usuarioId}")
    public List<Serie> listarTodas(@PathVariable Long usuarioId) {
        return serieService.listarPorUsuario(usuarioId);
    }

    // Obtener series filtradas por comprobante (Para los selects en la pantalla de
    // venta)
    @GetMapping("/usuario/{usuarioId}/comprobante/{tipoComprobanteId}")
    public List<Serie> listarPorComprobante(
            @PathVariable Long usuarioId,
            @PathVariable Long tipoComprobanteId) {
        return serieService.listarPorComprobante(usuarioId, tipoComprobanteId);
    }

    // Crear o actualizar una serie
    @PostMapping
    public Serie guardar(@RequestBody Serie serie) {
        return serieService.guardar(serie);
    }

    // Eliminar configuración de serie
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        serieService.eliminar(id);
    }
}