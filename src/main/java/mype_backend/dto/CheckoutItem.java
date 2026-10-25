package mype_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CheckoutItem(
        @NotNull @Positive Long tiendaProductoId,
        @Min(1) @Max(1000) int quantity,
        @Size(max = 50) String color,
        @Size(max = 30) String size,
        @Size(max = 500) String customerImagePath,
        @Size(max = 500) String resultadoMockupPath,
        @Valid MockupPosition mockup) {

    public record MockupPosition(
            @DecimalMin("0.01") @DecimalMax("100.00") java.math.BigDecimal scale,
            @DecimalMin("0.00") @DecimalMax("100.00") java.math.BigDecimal x,
            @DecimalMin("0.00") @DecimalMax("100.00") java.math.BigDecimal y) {
    }
}
