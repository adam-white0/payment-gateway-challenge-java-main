package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.ProcessPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.YearMonth;

public class ExpiryYearMonthValidator implements
    ConstraintValidator<ValidExpiryYearMonth, ProcessPaymentRequest> {

  @Override
  public boolean isValid(ProcessPaymentRequest request, ConstraintValidatorContext context) {
    if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return true;
    }

    YearMonth expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
    if (expiry.isAfter(YearMonth.now())) {
      return true;
    } else {
      if (request.getExpiryYear() >= YearMonth.now().getYear()) {
        // if expiryYear is greater or equal to the current year,
        // attach constraint violation error message to expiryMonth in response
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Card expiry date must be in the future")
            .addPropertyNode("expiryMonth")
            .addConstraintViolation();
      } else {
        // if expiryYear is less than the current year,
        // attach constraint violation error message to expiryYear in respone
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Card expiry date must be in the future")
            .addPropertyNode("expiryYear")
            .addConstraintViolation();
      }

      return false;
    }
  }
}