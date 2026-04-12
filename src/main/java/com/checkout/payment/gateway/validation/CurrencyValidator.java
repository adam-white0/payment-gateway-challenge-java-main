package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.enums.SupportedCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    List<String> supportedCurrencies = Arrays.stream(SupportedCurrency.values())
        .map(Enum::name).toList();

    if (supportedCurrencies.contains(value)) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(
        "Currency must be one of: " + String.join(", ", supportedCurrencies)
    ).addConstraintViolation();

    return false;
  }
}

