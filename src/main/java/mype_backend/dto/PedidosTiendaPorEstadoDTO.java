package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidosTiendaPorEstadoDTO {
    private String estado;
    private Long cantidadPedidos;
    private BigDecimal montoPedidos;
    private BigDecimal subtotalProductos;
    private BigDecimal costoEnvio;
}
