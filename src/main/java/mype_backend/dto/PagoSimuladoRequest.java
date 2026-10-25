package mype_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PagoSimuladoRequest(
        @NotBlank
        @Size(max = 120)
        String token,

        @NotBlank
        @Pattern(regexp = "TARJETA|YAPE_PLIN")
        String paymentMethodId
) {
}
