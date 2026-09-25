package mype_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class VentaRequestDTO {
    private Long usuarioId;
    private Long socioNegocioId;
    private Long tipoComprobanteId;
    private Long tipoOperacionId;
    private Long monedaId;
    private Long tipoPagoId;
    private String serie;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal opGravadas;
    private BigDecimal opExoneradas;
    private BigDecimal opInafectas;
    private BigDecimal igv;
    private BigDecimal total;
    private List<VentaDetalleRequestDTO> detalles;
    private GuiaRemisionRequestDTO guiaRemision;
}
