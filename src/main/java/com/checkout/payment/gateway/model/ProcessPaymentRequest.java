package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.validation.ValidCurrency;
import com.checkout.payment.gateway.validation.ValidExpiryYearMonth;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@ValidExpiryYearMonth
public class ProcessPaymentRequest {

  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{14,19}", message = "Card number must be 14-19 digits")
  private String cardNumber;

  @NotNull(message = "Expiry month is required")
  @Min(1)
  @Max(12)
  private Integer expiryMonth;

  @NotNull(message = "Expiry year is required")
  private Integer expiryYear;

  @NotBlank(message = "Currency is required")
  @ValidCurrency
  private String currency;

  @NotNull(message = "Amount is required")
  private Integer amount;

  @NotBlank(message = "CVV is required")
  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3-4 digits")
  private String cvv;
}
