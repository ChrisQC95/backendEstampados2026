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
    // Card 1: Total de ventas del mes (todos los tipos)
    private BigDecimal totalVentasMes;
    private Double crecimientoIngresos;
    private Long cantidadVentasMes;

    // Card 2: Monto emitido solo en Facturas Electrónicas
    private BigDecimal montoFacturas;
    private Long cantidadFacturas;

    // Card 3: Monto emitido solo en Boletas de Venta
    private BigDecimal montoBoletas;
    private Long cantidadBoletas;

    // Card 4: Monto emitido en Notas de Venta (documentos internos)
    private BigDecimal montoNotasVenta;
    private Long cantidadNotasVenta;

    // Card 5: Catálogo — total de productos registrados
    private Long totalProductos;

    // Card 6: Socios de negocio — total de clientes/proveedores
    private Long totalSocios;

    private List<VentaRecienteDTO> ventasRecientes;
    private List<GraficoVentaDTO> graficoVentas;
}
