package com.checkout.payment.gateway.exception;

public class PaymentAuthorisationException extends RuntimeException{
  public PaymentAuthorisationException(String message) {
    super(message);
  }
}
