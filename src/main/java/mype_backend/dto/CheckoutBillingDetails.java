package mype_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutBillingDetails(
        @NotBlank @Pattern(regexp = "FACTURA|BOLETA") String invoiceType,
        @NotBlank @Pattern(regexp = "01|04|06|07|A4") String documentType,
        @NotBlank @Size(min = 8, max = 20) @Pattern(regexp = "[0-9A-Za-z-]+") String documentNumber,
        @Size(max = 255) String fullName,
        @Size(max = 255) String businessName,
        @Size(max = 255) String fiscalAddress,
        @NotBlank @Email @Size(max = 150) String email,
        @Pattern(regexp = "^$|\\d{6}") String fiscalUbigeo) {

    @AssertTrue(message = "Los datos de facturación no corresponden al tipo de comprobante seleccionado.")
    public boolean isBillingDataValid() {
        if ("FACTURA".equals(invoiceType)) {
            return "06".equals(documentType)
                    && documentNumber != null
                    && documentNumber.matches("\\d{11}")
                    && hasText(businessName)
                    && hasText(fiscalAddress);
        }
        if ("BOLETA".equals(invoiceType)) {
            if ("06".equals(documentType) || !hasText(fullName)) return false;
            return !"01".equals(documentType) || documentNumber.matches("\\d{8}");
        }
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
