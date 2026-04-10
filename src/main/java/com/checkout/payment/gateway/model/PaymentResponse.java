package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
  private UUID id;
  private PaymentStatus status;
  private String cardNumberLastFour;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private int amount;

  public static PaymentResponse createFromProcessedResponse(ProcessedPaymentResponse response) {
    return PaymentResponse.builder()
        .id(response.getId())
        .status(response.getStatus())
        .cardNumberLastFour(response.getCardNumberLastFour())
        .expiryMonth(response.getExpiryMonth())
        .expiryYear(response.getExpiryYear())
        .currency(response.getCurrency())
        .amount(response.getAmount())
        .build();
  }

  @Override
  public String toString() {
    return "PaymentResponse{" +
        "id=" + id +
        ", status=" + status +
        ", cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
