package mype_backend.controller;

import mype_backend.dto.PedidoFacturacionDTO;
import mype_backend.service.PedidoFacturacionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tienda/facturacion")
public class PedidoFacturacionController {

    private final PedidoFacturacionService pedidoFacturacionService;

    public PedidoFacturacionController(PedidoFacturacionService pedidoFacturacionService) {
        this.pedidoFacturacionService = pedidoFacturacionService;
    }

    @GetMapping("/pendientes")
    public List<PedidoFacturacionDTO> listarPendientes(@AuthenticationPrincipal Jwt jwt) {
        return pedidoFacturacionService.listarPendientes(jwt);
    }

    @GetMapping("/pendientes/seleccionados")
    public List<PedidoFacturacionDTO> obtenerSeleccionados(
            @RequestParam List<Long> ids,
            @AuthenticationPrincipal Jwt jwt) {
        return pedidoFacturacionService.obtenerSeleccionados(jwt, ids);
    }
}
