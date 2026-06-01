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
    private BigDecimal ingresosMesActual;
    private Double crecimientoIngresos;
    private Long cantidadVentasMes;
    private Long totalProductos;
    private Long totalCategorias;
    private Long totalSocios;
    private Long totalVehiculos;
    private Long totalConductores;
    
    private List<VentaRecienteDTO> ventasRecientes;
    private List<GraficoVentaDTO> graficoVentas;
}
