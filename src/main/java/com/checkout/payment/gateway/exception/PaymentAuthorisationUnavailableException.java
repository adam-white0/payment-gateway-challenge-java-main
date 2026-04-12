package com.checkout.payment.gateway.exception;

public class PaymentAuthorisationUnavailableException extends RuntimeException {

  public PaymentAuthorisationUnavailableException(String message) {
    super(message);
  }
}
