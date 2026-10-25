package mype_backend.controller;

import jakarta.validation.Valid;
import mype_backend.dto.CheckoutRequest;
import mype_backend.dto.CheckoutResponse;
import mype_backend.dto.PagoSimuladoRequest;
import mype_backend.dto.PagoSimuladoResponse;
import mype_backend.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tienda")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkoutService.createPendingOrder(jwt, request));
    }

    @PostMapping("/pedidos/{pedidoId}/pago")
    public ResponseEntity<PagoSimuladoResponse> procesarPagoSimulado(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PagoSimuladoRequest request) {
        return ResponseEntity.ok(checkoutService.procesarPagoSimulado(pedidoId, jwt, request));
    }
}
