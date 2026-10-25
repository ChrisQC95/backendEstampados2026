package mype_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoFacturacionDTO(
        Long pedidoId,
        String codigo,
        String estado,
        String estadoPago,
        String metodoPago,
        OffsetDateTime fechaCreacion,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        Long productoServicioEnvioId,
        String nombreServicioEnvio,
        BigDecimal total,
        String tipoComprobante,
        String tipoDocumento,
        String numeroDocumento,
        String nombreFacturacion,
        String direccionFiscal,
        String ubigeoFiscal,
        String emailFacturacion,
        String direccionEntrega,
        String ubigeoEntrega,
        List<Item> items) {

    public record Item(
            Long tiendaProductoId,
            Long productoServicioId,
            Long servicioEstampadoId,
            String productoNombre,
            String servicioNombre,
            String talla,
            Integer cantidad,
            BigDecimal precioProducto,
            BigDecimal precioServicio) {
    }
}
