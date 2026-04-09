package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthorisePaymentRequest {
  public String cardNumber;
  public String expiryDate;
  public String currency;
  public Integer amount;
  public String cvv;
}
