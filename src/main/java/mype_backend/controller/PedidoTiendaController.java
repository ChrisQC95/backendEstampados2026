package mype_backend.controller;

import mype_backend.dto.PedidoTiendaResumenDTO;
import mype_backend.service.PedidoTiendaGestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tienda/pedidos")
public class PedidoTiendaController {
    private final PedidoTiendaGestionService service;

    public PedidoTiendaController(PedidoTiendaGestionService service) {
        this.service = service;
    }

    @GetMapping("/mis")
    public List<PedidoTiendaResumenDTO> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        return service.listarMisPedidos(jwt);
    }

    @GetMapping("/gestion")
    public List<PedidoTiendaResumenDTO> pedidosParaGestion(@AuthenticationPrincipal Jwt jwt) {
        return service.listarParaGestion(jwt);
    }

    @PostMapping("/{pedidoId}/cancelar")
    public PedidoTiendaResumenDTO cancelar(@PathVariable Long pedidoId, @AuthenticationPrincipal Jwt jwt) {
        return service.cancelarPedido(pedidoId, jwt);
    }
}
