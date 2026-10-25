package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentasPorComprobanteDTO {
    private Long tipoComprobanteId;
    private String tipoComprobante;
    private Long cantidadVentas;
    private BigDecimal montoVentas;
}
