package mype_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoVentaDTO {
    private String name;          // Mes abreviado en español, ej: "Ene", "Feb"
    private BigDecimal facturas;  // Monto de Facturas Electrónicas en el mes
    private BigDecimal boletas;   // Monto de Boletas de Venta en el mes
    private BigDecimal notasVenta; // Monto de Notas de Venta en el mes
    private BigDecimal total;     // Suma de los tres tipos
}
