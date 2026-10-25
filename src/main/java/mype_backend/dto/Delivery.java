package mype_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Delivery(
        @NotBlank @Size(max = 255) String address,
        @Size(max = 255) String reference,
        @NotBlank @Size(max = 50) String zoneId,
        @NotBlank @Pattern(regexp = "\\d{6}") String ubigeo) {
}
