package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentasPorProductoDTO {
    private Long productoId;
    private String productoNombre;
    private String categoria;
    private BigDecimal unidadesVendidas;
    private BigDecimal montoVendido;
    private Long cantidadLineas;
}
