package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.PaymentInformationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public ResponseEntity<PaymentInformationErrorResponse> handleValidationException(
          MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
    );

    return new ResponseEntity<>(
            new PaymentInformationErrorResponse(
                    "Invalid details",
                    PaymentStatus.REJECTED,
                    errors
            ), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PaymentAuthorisationException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handlePaymentAuthorisationException(
      PaymentAuthorisationException ex){

    return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
  }

}
