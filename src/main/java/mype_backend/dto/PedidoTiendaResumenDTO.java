package mype_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoTiendaResumenDTO(
        Long pedidoId,
        String codigo,
        String estadoGestion,
        String estadoPedido,
        String estadoPago,
        String metodoPago,
        OffsetDateTime fechaCreacion,
        Long clienteId,
        String nombreCliente,
        String emailCliente,
        String tipoComprobante,
        String tipoDocumento,
        String numeroDocumento,
        String nombreFacturacion,
        String direccionEntrega,
        String ubigeoEntrega,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal total,
        List<Item> items) {

    public record Item(
            Long id,
            String productoNombre,
            String servicioNombre,
            Integer cantidad,
            String color,
            String talla,
            BigDecimal precioProducto,
            BigDecimal precioEstampado,
            BigDecimal subtotal,
            String imagenClientePath,
            String resultadoMockupPath) {
    }
}
