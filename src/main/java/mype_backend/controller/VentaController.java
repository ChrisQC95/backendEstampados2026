package mype_backend.controller;

import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentaRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    /**
     * Reporte Excel Maestro-Detalle.
     * Devuelve filas planas con datos de venta+detalle+cliente para un rango de fechas.
     * Ejemplo: GET /api/ventas/reporte?usuarioId=1&fechaInicio=2026-06-01&fechaFin=2026-06-30
     */
    @GetMapping("/reporte")
    public List<ReporteVentaExcelDTO> generarReporte(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ventaService.generarReporteExcel(usuarioId, fechaInicio, fechaFin);
    }
}