package mype_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record MockupProductoBaseResponseDTO(
        Long id,
        String nombre,
        String tipoProducto,
        String color,
        List<String> coloresDisponibles,
        String imagenBasePath,
        BigDecimal areaX,
        BigDecimal areaY,
        BigDecimal areaWidth,
        BigDecimal areaHeight,
        Boolean activo,
        OffsetDateTime creadoEn) {
}
