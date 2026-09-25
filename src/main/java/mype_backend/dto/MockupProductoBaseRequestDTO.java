package mype_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record MockupProductoBaseRequestDTO(
        String nombre,
        String tipoProducto,
        List<String> coloresDisponibles,
        BigDecimal areaX,
        BigDecimal areaY,
        BigDecimal areaWidth,
        BigDecimal areaHeight,
        Boolean activo) {
}
