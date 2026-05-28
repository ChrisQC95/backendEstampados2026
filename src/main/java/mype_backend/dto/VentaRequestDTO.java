package mype_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class VentaRequestDTO {
    // Claves foráneas necesarias
    private Long usuarioId;
    private Long socioNegocioId;
    private Long tipoComprobanteId;
    private Long tipoOperacionId;
    private Long monedaId;
    private Long tipoPagoId;

    // Datos del comprobante
    private String serie;
    // OJO: El 'correlativo' no viene de React. El backend lo calculará
    // automáticamente.

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    // Totales de la cabecera
    private BigDecimal opGravadas;
    private BigDecimal opExoneradas;
    private BigDecimal opInafectas;
    private BigDecimal igv;
    private BigDecimal total;

    // LA LISTA DE PRODUCTOS (El carrito)
    private List<VentaDetalleRequestDTO> detalles;
    private GuiaRemisionRequestDTO guiaRemision;
}