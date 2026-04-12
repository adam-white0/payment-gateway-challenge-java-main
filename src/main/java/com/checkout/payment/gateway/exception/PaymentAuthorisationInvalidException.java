package com.checkout.payment.gateway.exception;

public class PaymentAuthorisationInvalidException extends RuntimeException {

  public PaymentAuthorisationInvalidException(String message) {
    super(message);
  }
}
