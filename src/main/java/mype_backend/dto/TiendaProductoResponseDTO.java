package mype_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TiendaProductoResponseDTO(
        Long id,
        ProductoResumenDTO productoServicio,
        ProductoResumenDTO servicioEstampado,
        MockupProductoBaseResponseDTO mockupProductoBase,
        String slug,
        String descripcionTienda,
        Boolean publicado,
        Boolean destacado,
        Boolean permitePersonalizacion,
        Boolean manejaTallas,
        Integer orden,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn) {

    public record ProductoResumenDTO(
            Long id,
            String codigo,
            String nombre,
            String tipo,
            String unidadMedida,
            BigDecimal precioVenta,
            BigDecimal precioCompra,
            BigDecimal stockActual) {
    }
}
