package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
public class PaymentInformationErrorResponse {

  @Getter
  private final String message;

  private PaymentStatus paymentStatus;

  @Getter
  private Map<String, String> errors;

  public String getPaymentStatus() {
    return paymentStatus.getName();
  }
}
