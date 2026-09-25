package mype_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private BigDecimal totalVentasMes;
    private Double crecimientoIngresos;
    private Long cantidadVentasMes;
    private BigDecimal montoFacturas;
    private Long cantidadFacturas;
    private BigDecimal montoBoletas;
    private Long cantidadBoletas;
    private BigDecimal montoNotasVenta;
    private Long cantidadNotasVenta;
    private Long totalProductos;
    private Long totalSocios;
    private List<VentaRecienteDTO> ventasRecientes;
    private List<GraficoVentaDTO> graficoVentas;
}
