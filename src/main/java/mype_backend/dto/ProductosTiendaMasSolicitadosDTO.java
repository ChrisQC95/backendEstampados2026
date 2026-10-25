package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductosTiendaMasSolicitadosDTO {
    private Long tiendaProductoId;
    private String productoNombre;
    private String servicioEstampado;
    private Long cantidadUnidadesSolicitadas;
    private BigDecimal importeSolicitado;
    private Long cantidadPedidos;
}
