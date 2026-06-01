package mype_backend.controller;

import mype_backend.dto.DashboardDTO;
import mype_backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
