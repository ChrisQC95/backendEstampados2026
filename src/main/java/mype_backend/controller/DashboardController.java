package mype_backend.controller;

import mype_backend.dto.DashboardDTO;
import mype_backend.dto.ReportePedidosTiendaDTO;
import mype_backend.dto.VentasPorComprobanteDTO;
import mype_backend.dto.VentasPorProductoDTO;
import mype_backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
// @CrossOrigin(origins = "*") // Usualmente manejado globalmente
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumen")
    public DashboardDTO getResumen(@RequestParam Long usuarioId) {
        return dashboardService.getResumenDashboard(usuarioId);
    }

    @GetMapping("/reportes/ventas-por-comprobante")
    public List<VentasPorComprobanteDTO> getVentasPorComprobante(@RequestParam Long usuarioId) {
        return dashboardService.getVentasPorComprobante(usuarioId);
    }

    @GetMapping("/reportes/ventas-por-producto")
    public List<VentasPorProductoDTO> getVentasPorProducto(@RequestParam Long usuarioId) {
        return dashboardService.getVentasPorProducto(usuarioId);
    }

    @GetMapping("/reportes/pedidos-tienda")
    public ReportePedidosTiendaDTO getReportePedidosTienda(@RequestParam Long usuarioId) {
        return dashboardService.getReportePedidosTienda(usuarioId);
    }
}

