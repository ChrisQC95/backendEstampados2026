package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO plano para el reporte Excel Maestro-Detalle de Ventas.
 * Cada fila representa un ítem de detalle con los datos de su venta cabecera.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaExcelDTO {

    // --- Datos Maestro (Venta) ---
    private LocalDateTime fechaEmision;
    private String tipoComprobante;   // Ej: "FACTURA ELECTRÓNICA"
    private String serie;
    private Integer correlativo;

    // --- Datos del Cliente (SocioNegocio) ---
    private String rucCliente;
    private String razonSocialCliente;

    // --- Datos Detalle (VentaDetalle) ---
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;  // totalLinea del detalle

    // --- Estado SUNAT ---
    private String estadoSunat;
}
