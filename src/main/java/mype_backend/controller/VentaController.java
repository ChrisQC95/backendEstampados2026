package mype_backend.controller;

import mype_backend.dto.NotaCreditoRequestDTO;
import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentaRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    /** Registrar una nueva venta (factura, boleta o nota de venta) */
    @PostMapping
    public Venta crearVenta(@RequestBody VentaRequestDTO ventaRequestDTO) {
        return ventaService.registrarVenta(ventaRequestDTO);
    }

    /** Historial de ventas del usuario (incluye NC para mostrarlo en tabla) */
    @GetMapping("/usuario/{usuarioId}")
    public List<Venta> listarPorUsuario(@PathVariable Long usuarioId) {
        return ventaService.listarHistorial(usuarioId);
    }

    /**
     * Emitir una Nota de Crédito sobre una venta existente.
     * POST /api/ventas/{id}/nota-credito
     * Body: { "motivoNcCodigo": "01", "sustentoNota": "Anulación de la operación" }
     */
    @PostMapping("/{id}/nota-credito")
    public ResponseEntity<?> emitirNotaCredito(
            @PathVariable Long id,
            @RequestBody NotaCreditoRequestDTO dto) {
        try {
            Venta nc = ventaService.generarNotaCredito(id, dto);
            return ResponseEntity.ok(nc);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Reporte Excel Maestro-Detalle.
     * Excluye NC y ventas anuladas por NC.
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