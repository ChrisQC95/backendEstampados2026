package mype_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VentaDetalleRequestDTO {
    private Long productoId;
    private String productoNombre;
    private String unidadMedida;

    private BigDecimal cantidad;
    private BigDecimal precioUnitario; // Precio CON IGV
    private BigDecimal valorUnitario; // Precio SIN IGV
    private BigDecimal igvLinea;
    private BigDecimal totalLinea;
}
