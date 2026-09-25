package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaExcelDTO {
    private LocalDateTime fechaEmision;
    private String tipoComprobante;
    private String serie;
    private Integer correlativo;
    private String rucCliente;
    private String razonSocialCliente;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;
    private String estadoSunat;
}
