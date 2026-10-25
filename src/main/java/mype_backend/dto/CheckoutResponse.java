package mype_backend.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long pedidoId,
        String codigo,
        String estado,
        String estadoPago,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal total,
        String moneda) {
}
