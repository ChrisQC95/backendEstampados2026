package mype_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CheckoutRequest(
        @NotBlank
        @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
        String idempotencyKey,
        @AssertTrue boolean termsAccepted,
        @NotNull @Valid Delivery delivery,
        @NotNull @Valid CheckoutBillingDetails billingDetails,
        @NotNull PaymentMethod paymentMethod,
        @NotEmpty @Size(max = 50) List<@NotNull @Valid CheckoutItem> items) {
}
