package mype_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record TiendaProductoPublicoDTO(
        Long id,
        String slug,
        String nombre,
        String categoria,
        String descripcion,
        BigDecimal precioProducto,
        BigDecimal precioServicioEstampado,
        BigDecimal precioTotal,
        String etiqueta,
        Boolean destacado,
        Boolean permitePersonalizacion,
        Boolean manejaTallas,
        String imagenBasePath,
        List<String> coloresDisponibles,
        BigDecimal areaX,
        BigDecimal areaY,
        BigDecimal areaWidth,
        BigDecimal areaHeight) {
}
