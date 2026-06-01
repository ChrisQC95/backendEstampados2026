package mype_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoVentaDTO {
    private String name; // Mes, ej: "Jan", "Feb"
    private BigDecimal total;
}
